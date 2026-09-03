import { useQuery } from '@tanstack/react-query'
import { complianceAPI } from '../lib/api'

const CATEGORY_COLORS = {
  CAP_TABLE: { bg: 'bg-indigo-100', bar: 'bg-indigo-500', text: 'text-indigo-700' },
  DPIIT: { bg: 'bg-purple-100', bar: 'bg-purple-500', text: 'text-purple-700' },
  ESOP: { bg: 'bg-blue-100', bar: 'bg-blue-500', text: 'text-blue-700' },
  SHARE_STRUCTURE: { bg: 'bg-cyan-100', bar: 'bg-cyan-500', text: 'text-cyan-700' },
  VALUATION: { bg: 'bg-emerald-100', bar: 'bg-emerald-500', text: 'text-emerald-700' },
}

const CATEGORY_LABELS = {
  CAP_TABLE: '📊 Cap Table',
  DPIIT: '🏛️ DPIIT',
  ESOP: '👥 ESOP',
  SHARE_STRUCTURE: '📋 Share Structure',
  VALUATION: '💰 Valuation',
}

export default function ScoreChart({ companyId }) {
  const { data: history, isLoading } = useQuery({
    queryKey: ['scoreHistory', companyId],
    queryFn: () => complianceAPI.getScoreHistory(companyId).then(r => r.data),
    enabled: !!companyId,
  })

  if (isLoading) {
    return (
      <div className="bg-white rounded-xl border border-gray-200 p-6">
        <div className="animate-pulse space-y-4">
          <div className="h-4 bg-gray-200 rounded w-1/3"></div>
          <div className="h-8 bg-gray-200 rounded"></div>
          <div className="h-8 bg-gray-200 rounded"></div>
        </div>
      </div>
    )
  }

  if (!history || history.length === 0) {
    return (
      <div className="bg-white rounded-xl border border-gray-200 p-6">
        <h3 className="text-sm font-semibold text-gray-900 mb-4">Readiness Scores</h3>
        <p className="text-sm text-gray-500">No scores yet. Run a compliance check first.</p>
      </div>
    )
  }

  const latest = history[0]
  const previous = history.length > 1 ? history[1] : null

  return (
    <div className="bg-white rounded-xl border border-gray-200 p-6">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-sm font-semibold text-gray-900">Readiness Scores</h3>
        {previous && (
          <span className="text-xs text-gray-500">
            vs previous: {getOverallTrend(latest.overallScore, previous.overallScore)}
          </span>
        )}
      </div>

      {/* Overall Score */}
      <div className="mb-6 text-center">
        <div className="inline-flex items-center justify-center w-20 h-20 rounded-full bg-gray-50 border-4 border-gray-200">
          <span className="text-2xl font-bold text-gray-900">{latest.overallScore}%</span>
        </div>
        <p className="text-xs text-gray-500 mt-2">Overall Readiness</p>
        {previous && (
          <p className={`text-xs font-medium ${getTrendColor(latest.overallScore, previous.overallScore)}`}>
            {getTrendIcon(latest.overallScore, previous.overallScore)} {getTrendText(latest.overallScore, previous.overallScore)}
          </p>
        )}
      </div>

      {/* Category Bars */}
      <div className="space-y-3">
        {Object.entries(latest.categoryScores || {}).map(([category, score]) => {
          const colors = CATEGORY_COLORS[category] || CATEGORY_COLORS.CAP_TABLE
          const prevScore = previous?.categoryScores?.[category]
          const prevPercent = prevScore ? Number(prevScore) : null

          return (
            <div key={category}>
              <div className="flex items-center justify-between mb-1">
                <span className="text-xs font-medium text-gray-700">
                  {CATEGORY_LABELS[category] || category}
                </span>
                <div className="flex items-center gap-2">
                  <span className="text-xs font-semibold text-gray-900">{score}%</span>
                  {prevPercent !== null && (
                    <span className={`text-xs ${getTrendColor(Number(score), prevPercent)}`}>
                      {getTrendIcon(Number(score), prevPercent)}
                    </span>
                  )}
                </div>
              </div>
              <div className={`h-2 rounded-full ${colors.bg}`}>
                <div
                  className={`h-2 rounded-full ${colors.bar} transition-all duration-500`}
                  style={{ width: `${score}%` }}
                ></div>
              </div>
            </div>
          )
        })}
      </div>

      {/* History count */}
      {history.length > 1 && (
        <p className="text-xs text-gray-400 mt-4 text-center">
          {history.length} checks recorded
        </p>
      )}
    </div>
  )
}

function getOverallTrend(current, previous) {
  const diff = Number(current) - Number(previous)
  if (diff > 0) return `↑ +${diff.toFixed(0)}%`
  if (diff < 0) return `↓ ${diff.toFixed(0)}%`
  return '→ same'
}

function getTrendColor(current, previous) {
  const diff = Number(current) - Number(previous)
  if (diff > 0) return 'text-green-600'
  if (diff < 0) return 'text-red-600'
  return 'text-gray-500'
}

function getTrendIcon(current, previous) {
  const diff = Number(current) - Number(previous)
  if (diff > 0) return '↑'
  if (diff < 0) return '↓'
  return '→'
}

function getTrendText(current, previous) {
  const diff = Number(current) - Number(previous)
  if (diff > 0) return `+${diff.toFixed(0)}% from last check`
  if (diff < 0) return `${diff.toFixed(0)}% from last check`
  return 'Same as last check'
}
