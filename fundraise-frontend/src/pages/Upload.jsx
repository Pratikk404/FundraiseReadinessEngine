import { useState, useCallback } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { documentAPI, complianceAPI } from '../lib/api'

export default function Upload() {
  const { companyId } = useParams()
  const queryClient = useQueryClient()
  const [dragActive, setDragActive] = useState(false)
  const [uploadType, setUploadType] = useState('CAP_TABLE')

  const { data: documents, isLoading } = useQuery({
    queryKey: ['documents', companyId],
    queryFn: () => documentAPI.getDocuments(companyId).then(r => r.data),
  })

  const uploadMutation = useMutation({
    mutationFn: ({ file, type }) => documentAPI.upload(companyId, file, type),
    onSuccess: () => {
      queryClient.invalidateQueries(['documents', companyId])
    },
  })

  const runCheckMutation = useMutation({
    mutationFn: () => complianceAPI.runCheck(companyId),
    onSuccess: () => {
      queryClient.invalidateQueries(['findings', companyId])
      queryClient.invalidateQueries(['scores', companyId])
    },
  })

  const handleDrag = useCallback((e) => {
    e.preventDefault()
    e.stopPropagation()
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true)
    } else if (e.type === 'dragleave') {
      setDragActive(false)
    }
  }, [])

  const handleDrop = useCallback((e) => {
    e.preventDefault()
    e.stopPropagation()
    setDragActive(false)

    if (e.dataTransfer.files?.[0]) {
      uploadMutation.mutate({ file: e.dataTransfer.files[0], type: uploadType })
    }
  }, [uploadType, uploadMutation])

  const handleFileChange = (e) => {
    if (e.target.files?.[0]) {
      uploadMutation.mutate({ file: e.target.files[0], type: uploadType })
    }
  }

  const statusColor = {
    PENDING: 'bg-yellow-100 text-yellow-800',
    PROCESSING: 'bg-blue-100 text-blue-800',
    COMPLETED: 'bg-green-100 text-green-800',
    FAILED: 'bg-red-100 text-red-800',
  }

  const typeLabel = {
    CAP_TABLE: '📊 Cap Table (CSV/XLSX)',
    INCORPORATION: '📄 Incorporation Docs (PDF)',
    BOARD_RESOLUTION: '📋 Board Resolution (PDF)',
    FINANCIAL_STATEMENT: '💰 Financial Statement (PDF/XLSX)',
    SHA: '📜 SHA (PDF)',
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
      </div>
    )
  }

  return (
    <div>
      <div className="flex items-center gap-4 mb-8">
        <Link to="/" className="text-gray-400 hover:text-gray-600">← Back</Link>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Upload Documents</h1>
          <p className="text-sm text-gray-500">Upload cap tables and compliance documents</p>
        </div>
      </div>

      {/* Document Type Selector */}
      <div className="mb-6">
        <label className="block text-sm font-medium text-gray-700 mb-2">Document Type</label>
        <div className="flex flex-wrap gap-2">
          {Object.entries(typeLabel).map(([key, label]) => (
            <button
              key={key}
              onClick={() => setUploadType(key)}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                uploadType === key
                  ? 'bg-indigo-600 text-white'
                  : 'bg-white border border-gray-200 text-gray-700 hover:bg-gray-50'
              }`}
            >
              {label}
            </button>
          ))}
        </div>
      </div>

      {/* Upload Area */}
      <div
        onDragEnter={handleDrag}
        onDragLeave={handleDrag}
        onDragOver={handleDrag}
        onDrop={handleDrop}
        className={`border-2 border-dashed rounded-xl p-12 text-center transition-colors mb-8 ${
          dragActive
            ? 'border-indigo-500 bg-indigo-50'
            : 'border-gray-300 bg-white hover:border-gray-400'
        }`}
      >
        <div className="text-4xl mb-4">📁</div>
        <p className="text-lg font-medium text-gray-700 mb-2">
          {dragActive ? 'Drop your file here' : 'Drag & drop your file here'}
        </p>
        <p className="text-sm text-gray-500 mb-4">
          or click to browse
        </p>
        <input
          type="file"
          onChange={handleFileChange}
          className="hidden"
          id="file-upload"
          accept=".csv,.xlsx,.xls,.pdf,.docx"
        />
        <label
          htmlFor="file-upload"
          className="inline-block px-4 py-2 bg-gray-100 text-gray-700 rounded-lg text-sm font-medium hover:bg-gray-200 cursor-pointer"
        >
          Browse files
        </label>
        <p className="text-xs text-gray-400 mt-4">
          {uploadType === 'CAP_TABLE'
            ? 'Supported: CSV, XLSX, XLS (max 20MB)'
            : 'Supported: PDF, DOCX (max 20MB)'}
        </p>
      </div>

      {uploadMutation.isError && (
        <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg mb-6 text-sm">
          Upload failed: {uploadMutation.error.response?.data?.message || uploadMutation.error.message}
        </div>
      )}

      {uploadMutation.isSuccess && (
        <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg mb-6 text-sm">
          File uploaded successfully!
        </div>
      )}

      {/* Uploaded Documents */}
      {documents && documents.length > 0 && (
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-gray-900">Uploaded Documents</h2>
            <button
              onClick={() => runCheckMutation.mutate()}
              disabled={runCheckMutation.isPending}
              className="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50"
            >
              {runCheckMutation.isPending ? 'Running...' : '🔍 Run Compliance Check'}
            </button>
          </div>

          <div className="space-y-3">
            {documents.map((doc) => (
              <div key={doc.id} className="flex items-center gap-4 p-4 bg-gray-50 rounded-lg">
                <div className="text-2xl">
                  {doc.type === 'CAP_TABLE' ? '📊' : doc.type === 'INCORPORATION' ? '📄' : '📋'}
                </div>
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-900">{doc.filename}</p>
                  <p className="text-xs text-gray-500">
                    {doc.type.replace('_', ' ')} • Uploaded {new Date(doc.uploadDate).toLocaleString()}
                  </p>
                </div>
                <span className={`text-xs px-2 py-1 rounded-full font-medium ${statusColor[doc.processingStatus]}`}>
                  {doc.processingStatus}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
