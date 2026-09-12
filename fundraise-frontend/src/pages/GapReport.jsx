import { useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { complianceAPI } from '../lib/api'

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

  const readinessColor = {
    READY: 'bg-green-100 text-green-800 border-green-300',
    NEAR_READY: 'bg-yellow-100 text-yellow-800 border-yellow-300',
    NOT_READY: 'bg-red-100 text-red-800 border-red-300',
  }

  const readinessLabel = {
    READY: '✅ Ready for Fundraise',
    NEAR_READY: '⚠️ Near Ready',
    NOT_READY: '❌ Not Ready',
  }

  const severityColor = {
    CRITICAL: 'bg-red-50 border-red-200 text-red-800',
    WARNING: 'bg-yellow-50 border-yellow-200 text-yellow-800',
    INFO: 'bg-blue-50 border-blue-200 text-blue-800',
  }

  const severityIcon = {
    CRITICAL: '🔴',
    WARNING: '🟡',
    INFO: '🔵',
  }

  const effortColor = {
    QUICK_FIX: 'bg-green-100 text-green-700',
    MODERATE: 'bg-yellow-100 text-yellow-700',
    NEEDS_ADVISOR: 'bg-red-100 text-red-700',
  }

  const effortLabel = {
    QUICK_FIX: '⚡ Quick Fix',
    MODERATE: '🔧 Moderate',
    NEEDS_ADVISOR: '👨‍⚖️ Needs Advisor',
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="text-center py-20">
        <p className="text-red-600">Failed to load report. Run a compliance check first.</p>
        <Link to="/" className="text-indigo-600 mt-4 inline-block">← Back to Dashboard</Link>
      </div>
    )
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-8">
        <div className="flex items-center gap-4">
          <Link to="/" className="text-gray-400 hover:text-gray-600">← Back</Link>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Gap Report</h1>
            <p className="text-sm text-gray-500">{report?.companyName}</p>
          </div>
        </div>
        <button
          onClick={handlePrint}
          className="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700"
        >
          🖨️ Print / Export PDF
        </button>
      </div>

      {/* Readiness Badge */}
      <div className={`inline-block px-4 py-2 rounded-lg border-2 text-sm font-semibold mb-6 ${readinessColor[report?.readiness]}`}>
        {readinessLabel[report?.readiness]}
      </div>

      {/* Severity Summary */}
      <div className="grid grid-cols-3 gap-4 mb-8">
        <div className="bg-red-50 rounded-xl p-4 text-center">
          <div className="text-3xl font-bold text-red-600">{report?.severitySummary?.CRITICAL || 0}</div>
          <div className="text-sm text-red-700">Critical</div>
        </div>
        <div className="bg-yellow-50 rounded-xl p-4 text-center">
          <div className="text-3xl font-bold text-yellow-600">{report?.severitySummary?.WARNING || 0}</div>
          <div className="text-sm text-yellow-700">Warnings</div>
        </div>
        <div className="bg-blue-50 rounded-xl p-4 text-center">
          <div className="text-3xl font-bold text-blue-600">{report?.severitySummary?.INFO || 0}</div>
          <div className="text-sm text-blue-700">Info</div>
        </div>
      </div>

      {/* Priority Actions */}
      {report?.priorityActions?.length > 0 && (
        <div className="bg-green-50 border border-green-200 rounded-xl p-6 mb-8">
          <h2 className="text-lg font-semibold text-green-900 mb-4">🎯 Priority Actions</h2>
          <ol className="space-y-3">
            {report.priorityActions.map((action, i) => (
              <li key={i} className="flex gap-3">
                <span className="flex-shrink-0 w-6 h-6 bg-green-200 text-green-800 rounded-full flex items-center justify-center text-sm font-bold">
                  {i + 1}
                </span>
                <div>
                  <p className="font-medium text-green-900">{action.category}</p>
                  <p className="text-sm text-green-700">{action.action}</p>
                </div>
              </li>
            ))}
          </ol>
        </div>
      )}

      {/* Category Breakdown with Fix-It Guidance */}
      {report?.categoryBreakdown && Object.entries(report.categoryBreakdown).map(([category, findings]) => (
        <div key={category} className="mb-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-3">{formatCategory(category)}</h2>
          <div className="space-y-2">
            {findings.map((f) => {
              const guide = guides?.[f.ruleId]
              const isExpanded = expandedFinding === f.id

              return (
                <div key={f.id} className={`rounded-lg border ${severityColor[f.severity]}`}>
                  {/* Finding header — clickable */}
                  <button
                    onClick={() => toggleFinding(f.id)}
                    className="w-full p-4 text-left flex items-start gap-2 hover:bg-black/5 transition-colors"
                  >
                    <span>{severityIcon[f.severity]}</span>
                    <div className="flex-1">
                      <div className="flex items-center gap-2">
                        <p className="font-medium">{f.ruleId}</p>
                        {guide && (
                          <span className={`text-xs px-2 py-0.5 rounded-full ${effortColor[guide.effortLevel]}`}>
                            {effortLabel[guide.effortLevel]}
                          </span>
                        )}
                      </div>
                      <p className="text-sm mt-1 opacity-80">{f.description}</p>
                    </div>
                    <span className="text-gray-400 text-sm">{isExpanded ? '▲' : '▼'}</span>
                  </button>

                  {/* Fix-It Guidance — expandable */}
                  {isExpanded && guide && (
                    <div className="px-4 pb-4 border-t border-black/10">
                      {/* Why it matters */}
                      <div className="mt-4 p-3 bg-white rounded-lg border border-black/10">
                        <h4 className="text-sm font-semibold text-gray-900 mb-1">Why this matters</h4>
                        <p className="text-sm text-gray-700">{guide.whyItMatters}</p>
                      </div>

                      {/* How to fix */}
                      <div className="mt-3 p-3 bg-white rounded-lg border border-black/10">
                        <h4 className="text-sm font-semibold text-gray-900 mb-2">How to fix it</h4>
                        <ol className="space-y-2">
                          {guide.howToFix.map((step, i) => (
                            <li key={i} className="flex gap-2 text-sm text-gray-700">
                              <span className="flex-shrink-0 w-5 h-5 bg-indigo-100 text-indigo-700 rounded-full flex items-center justify-center text-xs font-bold">
                                {i + 1}
                              </span>
                              <span>{step}</span>
                            </li>
                          ))}
                        </ol>
                      </div>

                      {/* What good looks like */}
                      <div className="mt-3 p-3 bg-green-50 rounded-lg border border-green-200">
                        <h4 className="text-sm font-semibold text-green-900 mb-1">✅ What good looks like</h4>
                        <p className="text-sm text-green-700">{guide.whatGoodLooksLike}</p>
                      </div>

                      {/* Meta info */}
                      <div className="mt-3 flex items-center gap-4 text-xs text-gray-500">
                        <span>⏱️ {guide.estimatedTime}</span>
                        {guide.needsAdvisor && <span>👨‍⚖️ Professional help recommended</span>}
                      </div>

                      {/* Related cases */}
                      {guide.relatedCases?.length > 0 && (
                        <div className="mt-3 p-3 bg-gray-50 rounded-lg border border-gray-200">
                          <h4 className="text-sm font-semibold text-gray-900 mb-1">📚 Related cases</h4>
                          <ul className="space-y-1">
                            {guide.relatedCases.map((c, i) => (
                              <li key={i} className="text-sm text-gray-600">• {c}</li>
                            ))}
                          </ul>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              )
            })}
          </div>
        </div>
      ))}

      {/* Narrative */}
      {report?.narrative && (
        <div className="bg-gray-50 rounded-xl p-6 mt-8">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">📝 Detailed Analysis</h2>
          <div className="prose prose-sm max-w-none whitespace-pre-wrap text-gray-700">
            {report.narrative}
          </div>
        </div>
      )}
    </div>
  )
}

function formatCategory(category) {
  const icons = {
    CAP_TABLE: '📊',
    DPIIT: '🏛️',
    ESOP: '👥',
    SHARE_STRUCTURE: '📋',
    VALUATION: '💰',
  }
  return `${icons[category] || ''} ${category.replace(/_/g, ' ')}`
}
