import { useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { motion, AnimatePresence } from 'framer-motion'
import { complianceAPI } from '../lib/api'
import { Printer, ChevronDown, ChevronUp, AlertTriangle, XCircle, CheckCircle2, Clock, Shield, Zap } from 'lucide-react'

export default function GapReport() {
  const { companyId } = useParams()
  const [expandedFinding, setExpandedFinding] = useState(null)

  const { data: report, isLoading, error } = useQuery({
    queryKey: ['gapReport', companyId],
    queryFn: () => complianceAPI.getGapReport(companyId).then(r => r.data),
  })

  const { data: guides } = useQuery({
    queryKey: ['guides'],
    queryFn: () => complianceAPI.getAllGuides().then(r => r.data),
  })

  const handlePrint = async () => {
    try {
      const { data: html } = await complianceAPI.getPdfReport(companyId)
      const printWindow = window.open('', '_blank')
      printWindow.document.write(html)
      printWindow.document.close()
      printWindow.print()
    } catch (err) {
      console.error('Failed to load report:', err)
    }
  }

  const toggleFinding = (findingId) => {
    setExpandedFinding(expandedFinding === findingId ? null : findingId)
  }

  const readinessConfig = {
    READY: { bg: 'bg-green-500/10', text: 'text-green-400', border: 'border-green-500/20', label: '✅ Ready for Fundraise', gradient: 'from-green-500 to-emerald-500' },
    NEAR_READY: { bg: 'bg-amber-500/10', text: 'text-amber-400', border: 'border-amber-500/20', label: '⚠️ Near Ready', gradient: 'from-amber-500 to-yellow-500' },
    NOT_READY: { bg: 'bg-red-500/10', text: 'text-red-400', border: 'border-red-500/20', label: '❌ Not Ready', gradient: 'from-red-500 to-rose-500' },
  }

  const severityConfig = {
    CRITICAL: { bg: 'bg-red-50', border: 'border-red-200', text: 'text-red-700', icon: XCircle, dot: 'bg-red-500' },
    WARNING: { bg: 'bg-amber-50', border: 'border-amber-200', text: 'text-amber-700', icon: AlertTriangle, dot: 'bg-amber-500' },
    INFO: { bg: 'bg-blue-50', border: 'border-blue-200', text: 'text-blue-700', icon: CheckCircle2, dot: 'bg-blue-500' },
  }

  const effortConfig = {
    QUICK_FIX: { bg: 'bg-green-500/10', text: 'text-green-500', label: '⚡ Quick Fix', icon: Zap },
    MODERATE: { bg: 'bg-amber-500/10', text: 'text-amber-500', label: '🔧 Moderate', icon: Clock },
    NEEDS_ADVISOR: { bg: 'bg-red-500/10', text: 'text-red-500', label: '👨‍⚖️ Needs Advisor', icon: Shield },
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="w-10 h-10 border-3 border-indigo-200 border-t-indigo-600 rounded-full animate-spin" />
      </div>
    )
  }

  if (error) {
    return (
      <div className="text-center py-20">
        <div className="w-16 h-16 bg-red-50 rounded-2xl flex items-center justify-center mx-auto mb-4">
          <XCircle className="w-8 h-8 text-red-400" />
        </div>
        <p className="text-red-600 font-medium">Failed to load report</p>
        <p className="text-sm text-gray-400 mt-1">Run a compliance check first</p>
        <Link to="/app" className="mt-4 inline-flex items-center gap-1 text-sm text-indigo-600 hover:text-indigo-700 font-medium">
          ← Back to Dashboard
        </Link>
      </div>
    )
  }

  const readiness = readinessConfig[report?.readiness] || readinessConfig.NOT_READY

  return (
    <div>
      {/* Header */}
      <motion.div
        initial={{ opacity: 0, y: -10 }}
        animate={{ opacity: 1, y: 0 }}
        className="flex items-center justify-between mb-8"
      >
        <div className="flex items-center gap-4">
          <Link to="/app" className="text-gray-400 hover:text-gray-600 transition-colors">
            ← Back
          </Link>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Gap Report</h1>
            <p className="text-sm text-gray-500">{report?.companyName}</p>
          </div>
        </div>
        <motion.button
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          onClick={handlePrint}
          className="btn-primary flex items-center gap-2"
        >
          <Printer className="w-4 h-4" /> Export PDF
        </motion.button>
      </motion.div>

      {/* Readiness Badge */}
      <motion.div
        initial={{ opacity: 0, scale: 0.9 }}
        animate={{ opacity: 1, scale: 1 }}
        className={`inline-flex items-center gap-2 px-4 py-2 rounded-xl border ${readiness.bg} ${readiness.text} ${readiness.border} font-semibold text-sm mb-6`}
      >
        {readiness.label}
      </motion.div>

      {/* Severity Summary Cards */}
      <div className="grid grid-cols-3 gap-4 mb-8">
        {[
          { key: 'CRITICAL', icon: XCircle, label: 'Critical', color: 'from-red-500 to-rose-500' },
          { key: 'WARNING', icon: AlertTriangle, label: 'Warnings', color: 'from-amber-500 to-yellow-500' },
          { key: 'INFO', icon: CheckCircle2, label: 'Info', color: 'from-blue-500 to-cyan-500' },
        ].map((item, i) => (
          <motion.div
            key={item.key}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: i * 0.1 }}
            whileHover={{ y: -2 }}
            className="glass-card p-4 text-center"
          >
            <div className={`w-10 h-10 rounded-xl bg-gradient-to-br ${item.color} flex items-center justify-center mx-auto mb-2 shadow-lg`}>
              <item.icon className="w-5 h-5 text-white" />
            </div>
            <div className="text-2xl font-bold text-gray-900">{report?.severitySummary?.[item.key] || 0}</div>
            <div className="text-xs text-gray-500 font-medium">{item.label}</div>
          </motion.div>
        ))}
      </div>

      {/* Priority Actions */}
      {report?.priorityActions?.length > 0 && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="glass-card p-6 mb-8 border-l-4 border-green-500"
        >
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
            🎯 Priority Actions
          </h2>
          <ol className="space-y-3">
            {report.priorityActions.map((action, i) => (
              <motion.li
                key={i}
                initial={{ opacity: 0, x: -10 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: i * 0.1 }}
                className="flex gap-3 items-start"
              >
                <span className="flex-shrink-0 w-7 h-7 bg-gradient-to-br from-green-500 to-emerald-500 text-white rounded-lg flex items-center justify-center text-sm font-bold shadow-sm">
                  {i + 1}
                </span>
                <div>
                  <p className="font-semibold text-gray-900 text-sm">{action.category}</p>
                  <p className="text-sm text-gray-600">{action.action}</p>
                </div>
              </motion.li>
            ))}
          </ol>
        </motion.div>
      )}

      {/* Category Breakdown with Fix-It Guidance */}
      {report?.categoryBreakdown && Object.entries(report.categoryBreakdown).map(([category, findings], ci) => (
        <motion.div
          key={category}
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: ci * 0.1 }}
          className="mb-6"
        >
          <h2 className="text-lg font-semibold text-gray-900 mb-3 flex items-center gap-2">
            {formatCategory(category)}
          </h2>
          <div className="space-y-2">
            {findings.map((f, fi) => {
              const guide = guides?.[f.ruleId]
              const isExpanded = expandedFinding === f.id
              const config = severityConfig[f.severity]
              const effort = guide ? effortConfig[guide.effortLevel] : null

              return (
                <motion.div
                  key={f.id}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: fi * 0.05 }}
                  className="glass-card overflow-hidden"
                >
                  <button
                    onClick={() => toggleFinding(f.id)}
                    className="w-full p-4 text-left flex items-start gap-3 hover:bg-white/40 transition-all"
                  >
                    <div className={`w-8 h-8 rounded-lg ${config.bg} flex items-center justify-center flex-shrink-0`}>
                      <config.icon className={`w-4 h-4 ${config.text}`} />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 flex-wrap">
                        <p className="font-semibold text-gray-900 text-sm">{f.ruleId}</p>
                        {effort && (
                          <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${effort.bg} ${effort.text}`}>
                            {effort.label}
                          </span>
                        )}
                      </div>
                      <p className="text-sm text-gray-500 mt-1">{f.description}</p>
                    </div>
                    <motion.div
                      animate={{ rotate: isExpanded ? 180 : 0 }}
                      className="text-gray-400 flex-shrink-0 mt-1"
                    >
                      <ChevronDown className="w-5 h-5" />
                    </motion.div>
                  </button>

                  <AnimatePresence>
                    {isExpanded && guide && (
                      <motion.div
                        initial={{ height: 0, opacity: 0 }}
                        animate={{ height: 'auto', opacity: 1 }}
                        exit={{ height: 0, opacity: 0 }}
                        transition={{ duration: 0.3, ease: [0.4, 0, 0.2, 1] }}
                        className="overflow-hidden"
                      >
                        <div className="px-4 pb-4 space-y-3">
                          {/* Why it matters */}
                          <div className="p-4 bg-white/60 rounded-xl border border-white/60">
                            <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">Why this matters</h4>
                            <p className="text-sm text-gray-700 leading-relaxed">{guide.whyItMatters}</p>
                          </div>

                          {/* How to fix */}
                          <div className="p-4 bg-white/60 rounded-xl border border-white/60">
                            <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-3">How to fix it</h4>
                            <ol className="space-y-3">
                              {guide.howToFix.map((step, i) => (
                                <motion.li
                                  key={i}
                                  initial={{ opacity: 0, x: -10 }}
                                  animate={{ opacity: 1, x: 0 }}
                                  transition={{ delay: i * 0.08 }}
                                  className="flex gap-3 text-sm text-gray-700"
                                >
                                  <span className="flex-shrink-0 w-6 h-6 bg-gradient-to-br from-indigo-500 to-purple-500 text-white rounded-lg flex items-center justify-center text-xs font-bold shadow-sm">
                                    {i + 1}
                                  </span>
                                  <span className="leading-relaxed pt-0.5">{step}</span>
                                </motion.li>
                              ))}
                            </ol>
                          </div>

                          {/* What good looks like */}
                          <div className="p-4 bg-green-50 rounded-xl border border-green-200">
                            <h4 className="text-xs font-bold text-green-700 uppercase tracking-wider mb-2">✅ What good looks like</h4>
                            <p className="text-sm text-green-700 leading-relaxed">{guide.whatGoodLooksLike}</p>
                          </div>

                          {/* Meta */}
                          <div className="flex items-center gap-4 text-xs text-gray-400">
                            <span className="flex items-center gap-1"><Clock className="w-3 h-3" /> {guide.estimatedTime}</span>
                            {guide.needsAdvisor && <span className="flex items-center gap-1"><Shield className="w-3 h-3" /> Professional help recommended</span>}
                          </div>

                          {/* Related cases */}
                          {guide.relatedCases?.length > 0 && (
                            <div className="p-4 bg-gray-50 rounded-xl border border-gray-200">
                              <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">📚 Related cases</h4>
                              <ul className="space-y-1.5">
                                {guide.relatedCases.map((c, i) => (
                                  <li key={i} className="text-sm text-gray-600 flex items-start gap-2">
                                    <span className="text-gray-300 mt-1">•</span>
                                    {c}
                                  </li>
                                ))}
                              </ul>
                            </div>
                          )}
                        </div>
                      </motion.div>
                    )}
                  </AnimatePresence>
                </motion.div>
              )
            })}
          </div>
        </motion.div>
      ))}

      {/* Narrative */}
      {report?.narrative && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="glass-card p-6 mt-8"
        >
          <h2 className="text-lg font-semibold text-gray-900 mb-4">📝 Detailed Analysis</h2>
          <div className="prose prose-sm max-w-none whitespace-pre-wrap text-gray-600 leading-relaxed">
            {report.narrative}
          </div>
        </motion.div>
      )}
    </div>
  )
}

function formatCategory(category) {
  const icons = { CAP_TABLE: '📊', DPIIT: '🏛️', ESOP: '👥', SHARE_STRUCTURE: '📋', VALUATION: '💰' }
  return `${icons[category] || ''} ${category.replace(/_/g, ' ')}`
}
