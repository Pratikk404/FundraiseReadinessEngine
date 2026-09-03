import { Worker } from 'bullmq';
import { MongoDB } from 'mongodb';
import { readFile } from 'fs/promises';
import pdfParse from 'pdf-parse/lib/pdf-parse.js';

const REDIS_URL = process.env.REDIS_URL || 'redis://localhost:6379';
const MONGO_URL = process.env.MONGO_URL || 'mongodb://fundraise:fundraise123@localhost:27017/fundraise_docs?authSource=admin';
const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

let mongoDb;

// Connect to MongoDB
async function connectMongo() {
  const client = new MongoDB(MONGO_URL);
  await client.connect();
  mongoDb = client.db('fundraise_docs');
  console.log('Connected to MongoDB');
  return client;
}

// Process a document job
async function processDocument(job) {
  const { documentId, companyId, storageUrl, documentType } = job.data;
  console.log(`Processing document: ${documentId} (${documentType})`);

  try {
    let rawText = '';
    let extractionMethod = 'unknown';

    // Extract text based on file type
    if (documentType === 'CAP_TABLE') {
      // CSV/XLSX parsing is handled by the Java backend
      rawText = '[Cap table parsing handled by backend]';
      extractionMethod = 'backend';
    } else if (storageUrl.endsWith('.pdf')) {
      const pdfBuffer = await readFile(storageUrl);
      const pdfData = await pdfParse(pdfBuffer);
      rawText = pdfData.text;
      extractionMethod = 'pdf-parse';
    }

    // Store raw text in MongoDB
    await mongoDb.collection('parsed_documents').insertOne({
      documentId,
      companyId,
      rawText,
      ocrMetadata: {
        engine: extractionMethod,
        confidence: extractionMethod === 'pdf-parse' ? 0.9 : 1.0,
        processingTime: `${Date.now() - job.timestamp}ms`
      },
      createdAt: new Date()
    });

    // Extract entities (placeholder for NLP/LLM integration)
    const entities = extractEntities(rawText, documentType);

    await mongoDb.collection('extracted_entities').insertOne({
      documentId,
      companyId,
      documentType,
      entities,
      extractionMethod: 'regex',
      confidence: 0.85,
      createdAt: new Date()
    });

    // Notify backend that processing is complete
    await fetch(`${BACKEND_URL}/api/compliance/check/${companyId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    });

    console.log(`Document ${documentId} processed successfully`);
    return { status: 'completed', documentId };

  } catch (error) {
    console.error(`Failed to process document ${documentId}:`, error.message);
    throw error;
  }
}

// Basic entity extraction using regex patterns
function extractEntities(text, documentType) {
  const entities = {};

  if (documentType === 'BOARD_RESOLUTION') {
    // Extract date patterns
    const dateMatch = text.match(/(?:dated|on|date)[:\s]*(\d{1,2}[\/\-]\d{1,2}[\/\-]\d{2,4})/i);
    if (dateMatch) entities.date = dateMatch[1];

    // Extract resolution references
    const resolutionMatch = text.match(/resolution\s*(?:no|number|#)[:\s]*(\d+)/i);
    if (resolutionMatch) entities.resolutionNumber = resolutionMatch[1];
  }

  if (documentType === 'SHA') {
    // Extract clause references
    const clauseMatches = text.match(/clause\s*(\d+(?:\.\d+)*)/gi);
    if (clauseMatches) entities.clauses = clauseMatches;
  }

  return entities;
}

// Create BullMQ worker
async function startWorker() {
  const mongoClient = await connectMongo();

  const worker = new Worker('document-processing', processDocument, {
    connection: { url: REDIS_URL },
    concurrency: 2,
    limiter: {
      max: 10,
      duration: 1000
    }
  });

  worker.on('completed', (job) => {
    console.log(`Job ${job.id} completed for document ${job.data.documentId}`);
  });

  worker.on('failed', (job, err) => {
    console.error(`Job ${job.id} failed:`, err.message);
  });

  worker.on('error', (err) => {
    console.error('Worker error:', err);
  });

  console.log('Document processing worker started');
  console.log(`Listening on Redis: ${REDIS_URL}`);
  console.log(`MongoDB: ${MONGO_URL}`);

  // Graceful shutdown
  process.on('SIGTERM', async () => {
    console.log('Shutting down worker...');
    await worker.close();
    await mongoClient.close();
    process.exit(0);
  });
}

startWorker().catch(console.error);
