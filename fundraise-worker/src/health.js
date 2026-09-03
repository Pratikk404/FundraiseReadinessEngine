import express from 'express';

const app = express();
const PORT = process.env.PORT || 3001;

app.get('/health', (req, res) => {
  res.json({
    status: 'UP',
    service: 'Fundraise Document Worker',
    version: '1.0.0',
    timestamp: new Date().toISOString()
  });
});

app.get('/queue/status', async (req, res) => {
  // Queue status would be checked here via BullMQ
  res.json({
    queue: 'document-processing',
    status: 'active'
  });
});

app.listen(PORT, () => {
  console.log(`Worker health endpoint: http://localhost:${PORT}/health`);
});

export default app;
