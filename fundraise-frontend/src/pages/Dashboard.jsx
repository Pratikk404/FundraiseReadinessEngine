import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { motion, AnimatePresence } from 'framer-motion'
import { companyAPI, complianceAPI } from '../lib/api'
import ScoreChart from '../components/ScoreChart'
import { SkeletonDashboard } from '../components/ui/Skeleton'
import { Plus, Play, FileText, Upload, AlertTriangle, CheckCircle2, XCircle, ChevronRight } from 'lucide-react'

export default function Dashboard() {
  const queryClient = useQueryClient()
  const [selectedCompany, setSelectedCompany] = useState(null)
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [newCompany, setNewCompany] = useState({ name: '', incorporationDate: '', dpiitStatus: 'UNKNOWN' })

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
      queryClient.invalidateQueries({ queryKey: ['findings', selectedCompany] })
      queryClient.invalidateQueries({ queryKey: ['scores', selectedCompany] })
      queryClient.invalidateQueries({ queryKey: ['companies'] })
    },
  })

  const createCompanyMutation = useMutation({
    mutationFn: (data) => companyAPI.createCompany(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['companies'] })
      setShowCreateModal(false)
      setNewCompany({ name: '', incorporationDate: '', dpiitStatus: 'UNKNOWN' })
    },
  })

  const severityConfig = {
    CRITICAL: { color: 'from-red-500 to-rose-500', bg: 'bg-red-50', text: 'text-red-700', icon: XCircle },
    WARNING: { color: 'from-amber-500 to-yellow-500', bg: 'bg-amber-50', text: 'text-amber-700', icon: AlertTriangle },
    INFO: { color: 'from-blue-500 to-cyan-500', bg: 'bg-blue-50', text: 'text-blue-700', icon: CheckCircle2 },
  }

  const dpiitConfig = {
    RECOGNIZED: { bg: 'bg-green-500/10', text: 'text-green-400', border: 'border-green-500/20' },
    NOT_RECOGNIZED: { bg: 'bg-red-500/10', text: 'text-red-400', border: 'border-red-500/20' },
    UNKNOWN: { bg: 'bg-gray-500/10', text: 'text-gray-400', border: 'border-gray-500/20' },
  }

  if (isLoading) return <SkeletonDashboard />

  return (
    <div>
      {/* Header */}
      <motion.div
        initial={{ opacity: 0, y: -10 }}
        animate={{ opacity: 1, y: 0 }}
        className="flex items-center justify-between mb-8"
      >
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
          <p className="text-sm text-gray-500 mt-1">Manage your companies and compliance checks</p>
        </div>
        <motion.button
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          onClick={() => setShowCreateModal(true)}
          className="btn-primary flex items-center gap-2"
        >
          <Plus className="w-4 h-4" /> New Company
        </motion.button>
      </motion.div>

      {/* Company Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
        {companies?.map((company, i) => (
          <motion.div
            key={company.id}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: i * 0.08 }}
            whileHover={{ y: -4 }}
            onClick={() => setSelectedCompany(company.id)}
            className={`glass-card p-5 cursor-pointer transition-all duration-300 ${
              selectedCompany === company.id
                ? 'ring-2 ring-indigo-500 shadow-xl shadow-indigo-500/10'
                : 'hover:shadow-lg'
            }`}
          >
            <div className="flex items-start justify-between mb-3">
              <h3 className="font-semibold text-gray-900 text-sm">{company.name}</h3>
              <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${dpiitConfig[company.dpiitStatus]?.bg} ${dpiitConfig[company.dpiitStatus]?.text} border ${dpiitConfig[company.dpiitStatus]?.border}`}>
                {company.dpiitStatus}
              </span>
            </div>

            {company.incorporationDate && (
              <p className="text-xs text-gray-400 mb-3">
                Inc: {new Date(company.incorporationDate).toLocaleDateString()}
              </p>
            )}

            <div className="flex gap-2 mb-4">
              {company.criticalFindings > 0 && (
                <span className="text-xs px-2 py-1 rounded-lg bg-red-50 text-red-600 font-medium flex items-center gap-1">
                  <XCircle className="w-3 h-3" /> {company.criticalFindings} critical
                </span>
              )}
              {company.warningFindings > 0 && (
                <span className="text-xs px-2 py-1 rounded-lg bg-amber-50 text-amber-600 font-medium flex items-center gap-1">
                  <AlertTriangle className="w-3 h-3" /> {company.warningFindings} warnings
                </span>
              )}
              {company.criticalFindings === 0 && company.warningFindings === 0 && (
                <span className="text-xs px-2 py-1 rounded-lg bg-green-50 text-green-600 font-medium flex items-center gap-1">
                  <CheckCircle2 className="w-3 h-3" /> All clear
                </span>
              )}
            </div>

            <div className="flex gap-2">
              <Link
                to={`/app/upload/${company.id}`}
                onClick={(e) => e.stopPropagation()}
                className="flex items-center gap-1 text-xs bg-white/60 hover:bg-white text-gray-600 px-3 py-1.5 rounded-lg border border-gray-200/50 transition-all"
              >
                <Upload className="w-3 h-3" /> Upload
              </Link>
              <Link
                to={`/app/report/${company.id}`}
                onClick={(e) => e.stopPropagation()}
                className="flex items-center gap-1 text-xs bg-indigo-50 hover:bg-indigo-100 text-indigo-600 px-3 py-1.5 rounded-lg transition-all"
              >
                <FileText className="w-3 h-3" /> Report
              </Link>
              <motion.button
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                onClick={(e) => {
                  e.stopPropagation()
                  runCheckMutation.mutate(company.id)
                }}
                disabled={runCheckMutation.isPending}
                className="flex items-center gap-1 text-xs bg-gradient-to-r from-indigo-500 to-purple-500 text-white px-3 py-1.5 rounded-lg shadow-sm disabled:opacity-50"
              >
                <Play className="w-3 h-3" />
                {runCheckMutation.isPending ? '...' : 'Run Check'}
              </motion.button>
            </div>
          </motion.div>
        ))}

        {companies?.length === 0 && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="col-span-full glass-card p-12 text-center"
          >
            <div className="w-16 h-16 bg-indigo-50 rounded-2xl flex items-center justify-center mx-auto mb-4">
              <Plus className="w-8 h-8 text-indigo-400" />
            </div>
            <p className="text-gray-500 font-medium">No companies yet</p>
            <p className="text-sm text-gray-400 mt-1">Create one to get started with compliance checks</p>
          </motion.div>
        )}
      </div>

      {/* Score Chart + Findings */}
      <AnimatePresence>
        {selectedCompany && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8"
          >
            <div className="lg:col-span-1">
              <ScoreChart companyId={selectedCompany} />
            </div>
            <div className="lg:col-span-2">
              <div className="glass-card p-6">
                <div className="flex items-center justify-between mb-4">
                  <h2 className="text-lg font-semibold text-gray-900">Findings</h2>
                  {scores && scores.length > 0 && (
                    <div className="flex gap-2">
                      {scores.map((s) => (
                        <span key={s.category} className="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded-lg font-medium">
                          {s.category}: {s.score}%
                        </span>
                      ))}
                    </div>
                  )}
                </div>

                {!findings || findings.length === 0 ? (
                  <div className="text-center py-12">
                    <div className="w-14 h-14 bg-green-50 rounded-2xl flex items-center justify-center mx-auto mb-3">
                      <CheckCircle2 className="w-7 h-7 text-green-400" />
                    </div>
                    <p className="text-gray-500 font-medium">No findings!</p>
                    <p className="text-sm text-gray-400 mt-1">Run a compliance check to see results</p>
                  </div>
                ) : (
                  <div className="space-y-2">
                    {findings.map((f, i) => {
                      const config = severityConfig[f.severity]
                      return (
                        <motion.div
                          key={f.id}
                          initial={{ opacity: 0, x: -10 }}
                          animate={{ opacity: 1, x: 0 }}
                          transition={{ delay: i * 0.05 }}
                          className="flex items-start gap-3 p-3 rounded-xl bg-white/50 border border-white/60 hover:bg-white/80 transition-all"
                        >
                          <div className={`w-8 h-8 rounded-lg ${config.bg} flex items-center justify-center flex-shrink-0`}>
                            <config.icon className={`w-4 h-4 ${config.text}`} />
                          </div>
                          <div className="flex-1 min-w-0">
                            <p className="text-sm text-gray-900 font-medium">{f.description}</p>
                            <p className="text-xs text-gray-400 mt-1">
                              {f.ruleId} • {f.category}
                            </p>
                          </div>
                          {!f.resolved && (
                            <motion.button
                              whileHover={{ scale: 1.05 }}
                              whileTap={{ scale: 0.95 }}
                              onClick={() => {
                                complianceAPI.resolveFinding(f.id).then(() => {
                                  queryClient.invalidateQueries({ queryKey: ['findings', selectedCompany] })
                                  queryClient.invalidateQueries({ queryKey: ['companies'] })
                                })
                              }}
                              className="text-xs text-green-600 hover:text-green-700 font-medium flex items-center gap-1 flex-shrink-0"
                            >
                              <CheckCircle2 className="w-3 h-3" /> Resolve
                            </motion.button>
                          )}
                        </motion.div>
                      )
                    })}
                  </div>
                )}
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Create Company Modal */}
      <AnimatePresence>
        {showCreateModal && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4"
            onClick={() => setShowCreateModal(false)}
          >
            <motion.div
              initial={{ opacity: 0, scale: 0.9, y: 20 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.9, y: 20 }}
              transition={{ type: 'spring', stiffness: 300, damping: 25 }}
              onClick={(e) => e.stopPropagation()}
              className="glass rounded-2xl p-6 w-full max-w-md"
            >
              <h3 className="text-lg font-semibold text-gray-900 mb-5">Create New Company</h3>
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
                    className="glass-input w-full"
                    placeholder="My Startup Pvt Ltd"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Incorporation Date</label>
                  <input
                    type="date"
                    value={newCompany.incorporationDate}
                    onChange={(e) => setNewCompany({ ...newCompany, incorporationDate: e.target.value })}
                    className="glass-input w-full"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">DPIIT Status</label>
                  <select
                    value={newCompany.dpiitStatus}
                    onChange={(e) => setNewCompany({ ...newCompany, dpiitStatus: e.target.value })}
                    className="glass-input w-full"
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
                    className="flex-1 btn-ghost"
                  >
                    Cancel
                  </button>
                  <motion.button
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                    type="submit"
                    disabled={createCompanyMutation.isPending}
                    className="flex-1 btn-primary flex items-center justify-center gap-2"
                  >
                    {createCompanyMutation.isPending ? (
                      <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                    ) : 'Create'}
                  </motion.button>
                </div>
              </form>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  )
}
