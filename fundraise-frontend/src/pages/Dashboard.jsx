import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { companyAPI, complianceAPI } from '../lib/api'

export default function Dashboard() {
  const queryClient = useQueryClient()
  const [selectedCompany, setSelectedCompany] = useState(null)
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [newCompany, setNewCompany] = useState({
    name: '',
    incorporationDate: '',
    dpiitStatus: 'UNKNOWN'
  })

  const { data: companies, isLoading } = useQuery({
    queryKey: ['companies'],
    queryFn: () => companyAPI.getMyCompanies().then(r => r.data),
  })

  const { data: findings } = useQuery({
    queryKey: ['findings', selectedCompany],
    queryFn: () => complianceAPI.getFindings(selectedCompany).then(r => r.data),
    enabled: !!selectedCompany,
  })

  const { data: scores } = useQuery({
    queryKey: ['scores', selectedCompany],
    queryFn: () => complianceAPI.getScores(selectedCompany).then(r => r.data),
    enabled: !!selectedCompany,
  })

  const runCheckMutation = useMutation({
    mutationFn: (companyId) => complianceAPI.runCheck(companyId),
    onSuccess: () => {
      queryClient.invalidateQueries(['findings', selectedCompany])
      queryClient.invalidateQueries(['scores', selectedCompany])
    },
  })

  const createCompanyMutation = useMutation({
    mutationFn: (data) => companyAPI.createCompany(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['companies'])
      setShowCreateModal(false)
      setNewCompany({ name: '', incorporationDate: '', dpiitStatus: 'UNKNOWN' })
    },
  })

  const severityColor = {
    CRITICAL: 'bg-red-100 text-red-800',
    WARNING: 'bg-yellow-100 text-yellow-800',
    INFO: 'bg-blue-100 text-blue-800',
  }

  const dpiitColor = {
    RECOGNIZED: 'bg-green-100 text-green-800',
    NOT_RECOGNIZED: 'bg-red-100 text-red-800',
    UNKNOWN: 'bg-gray-100 text-gray-800',
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
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
          <p className="text-sm text-gray-500">Manage your companies and compliance checks</p>
        </div>
        <button
          onClick={() => setShowCreateModal(true)}
          className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 text-sm font-medium"
        >
          + New Company
        </button>
      </div>

      {/* Company Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
        {companies?.map((company) => (
          <div
            key={company.id}
            onClick={() => setSelectedCompany(company.id)}
            className={`bg-white rounded-xl border-2 p-5 cursor-pointer transition-all hover:shadow-md ${
              selectedCompany === company.id
                ? 'border-indigo-500 shadow-md'
                : 'border-gray-200 hover:border-gray-300'
            }`}
          >
            <div className="flex items-start justify-between mb-3">
              <h3 className="font-semibold text-gray-900 text-sm">{company.name}</h3>
              <span className={`text-xs px-2 py-1 rounded-full ${dpiitColor[company.dpiitStatus]}`}>
                {company.dpiitStatus}
              </span>
            </div>

            {company.incorporationDate && (
              <p className="text-xs text-gray-500 mb-3">
                Inc: {new Date(company.incorporationDate).toLocaleDateString()}
              </p>
            )}

            <div className="flex gap-3">
              {company.criticalFindings > 0 && (
                <span className="text-xs bg-red-50 text-red-700 px-2 py-1 rounded">
                  {company.criticalFindings} critical
                </span>
              )}
              {company.warningFindings > 0 && (
                <span className="text-xs bg-yellow-50 text-yellow-700 px-2 py-1 rounded">
                  {company.warningFindings} warnings
                </span>
              )}
              {company.criticalFindings === 0 && company.warningFindings === 0 && (
                <span className="text-xs bg-green-50 text-green-700 px-2 py-1 rounded">
                  All clear
                </span>
              )}
            </div>

            <div className="mt-4 flex gap-2">
              <Link
                to={`/upload/${company.id}`}
                onClick={(e) => e.stopPropagation()}
                className="text-xs bg-gray-100 text-gray-700 px-3 py-1.5 rounded-lg hover:bg-gray-200"
              >
                Upload Docs
              </Link>
              <button
                onClick={(e) => {
                  e.stopPropagation()
                  runCheckMutation.mutate(company.id)
                }}
                disabled={runCheckMutation.isPending}
                className="text-xs bg-indigo-50 text-indigo-700 px-3 py-1.5 rounded-lg hover:bg-indigo-100"
              >
                {runCheckMutation.isPending ? 'Checking...' : 'Run Check'}
              </button>
            </div>
          </div>
        ))}

        {companies?.length === 0 && (
          <div className="col-span-full bg-white rounded-xl border border-dashed border-gray-300 p-12 text-center">
            <p className="text-gray-500">No companies yet. Create one to get started.</p>
          </div>
        )}
      </div>

      {/* Findings Panel */}
      {selectedCompany && findings && (
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-gray-900">Findings</h2>
            {scores && scores.length > 0 && (
              <div className="flex gap-2">
                {scores.map((s) => (
                  <span key={s.category} className="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded">
                    {s.category}: {s.score}%
                  </span>
                ))}
              </div>
            )}
          </div>

          {findings.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              <p className="text-lg mb-2">No findings!</p>
              <p className="text-sm">All compliance checks passed.</p>
            </div>
          ) : (
            <div className="space-y-3">
              {findings.map((f) => (
                <div key={f.id} className="flex items-start gap-3 p-3 bg-gray-50 rounded-lg">
                  <span className={`text-xs px-2 py-1 rounded-full font-medium ${severityColor[f.severity]}`}>
                    {f.severity}
                  </span>
                  <div className="flex-1">
                    <p className="text-sm text-gray-900">{f.description}</p>
                    <p className="text-xs text-gray-500 mt-1">
                      Rule: {f.ruleId} • Category: {f.category}
                    </p>
                  </div>
                  {!f.resolved && (
                    <button
                      onClick={() => {
                        complianceAPI.resolveFinding(f.id).then(() => {
                          queryClient.invalidateQueries(['findings', selectedCompany])
                          queryClient.invalidateQueries(['companies'])
                        })
                      }}
                      className="text-xs text-green-600 hover:text-green-700"
                    >
                      Resolve
                    </button>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Create Company Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 w-full max-w-md mx-4">
            <h3 className="text-lg font-semibold mb-4">Create New Company</h3>
            <form onSubmit={(e) => {
              e.preventDefault()
              createCompanyMutation.mutate(newCompany)
            }} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Company Name</label>
                <input
                  type="text"
                  required
                  value={newCompany.name}
                  onChange={(e) => setNewCompany({ ...newCompany, name: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500"
                  placeholder="My Startup Pvt Ltd"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Incorporation Date</label>
                <input
                  type="date"
                  value={newCompany.incorporationDate}
                  onChange={(e) => setNewCompany({ ...newCompany, incorporationDate: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">DPIIT Status</label>
                <select
                  value={newCompany.dpiitStatus}
                  onChange={(e) => setNewCompany({ ...newCompany, dpiitStatus: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500"
                >
                  <option value="UNKNOWN">Unknown</option>
                  <option value="RECOGNIZED">Recognized</option>
                  <option value="NOT_RECOGNIZED">Not Recognized</option>
                </select>
              </div>
              <div className="flex gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={createCompanyMutation.isPending}
                  className="flex-1 px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700 disabled:opacity-50"
                >
                  {createCompanyMutation.isPending ? 'Creating...' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
