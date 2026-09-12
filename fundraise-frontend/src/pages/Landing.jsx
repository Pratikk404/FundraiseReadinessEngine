import { Link } from 'react-router-dom'

export default function Landing() {
  return (
    <div className="min-h-screen bg-white">
      {/* Nav */}
      <nav className="border-b border-gray-100">
        <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 bg-indigo-600 rounded-lg flex items-center justify-center">
              <span className="text-white font-bold text-sm">FR</span>
            </div>
            <span className="text-xl font-bold text-gray-900">FundraiseReady</span>
          </div>
          <div className="flex items-center gap-4">
            <Link to="/login" className="text-sm text-gray-600 hover:text-gray-900">Login</Link>
            <Link to="/register" className="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700">
              Get Started Free
            </Link>
          </div>
        </div>
      </nav>

      {/* Hero */}
      <section className="max-w-7xl mx-auto px-6 py-20 text-center">
        <div className="inline-block px-4 py-1 bg-indigo-50 text-indigo-700 rounded-full text-sm font-medium mb-6">
          For Indian Startups Preparing to Fundraise
        </div>
        <h1 className="text-5xl font-bold text-gray-900 mb-6 leading-tight">
          Know Your Fundraise<br />
          <span className="text-indigo-600">Readiness Score</span>
        </h1>
        <p className="text-xl text-gray-600 mb-8 max-w-2xl mx-auto">
          Upload your cap table and compliance documents. Get instant analysis against
          DPIIT, FEMA, ESOP, and valuation rules that investors actually check.
        </p>
        <div className="flex items-center justify-center gap-4">
          <Link to="/register" className="px-8 py-3 bg-indigo-600 text-white rounded-lg font-medium hover:bg-indigo-700 text-lg">
            Start Free →
          </Link>
          <a href="#how-it-works" className="px-8 py-3 border border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-50">
            How It Works
          </a>
        </div>
        <p className="text-sm text-gray-400 mt-4">No credit card required • 1 company free tier</p>
      </section>

      {/* Problem */}
      <section className="bg-gray-50 py-20">
        <div className="max-w-7xl mx-auto px-6">
          <h2 className="text-3xl font-bold text-center text-gray-900 mb-12">The Problem</h2>
          <div className="grid md:grid-cols-3 gap-8">
            <div className="bg-white rounded-xl p-6 border border-gray-200">
              <div className="text-3xl mb-4">😰</div>
              <h3 className="font-semibold text-gray-900 mb-2">Manual Compliance Checks</h3>
              <p className="text-sm text-gray-600">Advisory firms charge ₹50,000-2,00,000 to manually verify your documents against regulatory patterns.</p>
            </div>
            <div className="bg-white rounded-xl p-6 border border-gray-200">
              <div className="text-3xl mb-4"> spreadsheet</div>
              <h3 className="font-semibold text-gray-900 mb-2">Spreadsheet Drift</h3>
              <p className="text-sm text-gray-600">Cap tables get out of sync. ESOP promises are forgotten. Dilution math doesn't add up to 100%.</p>
            </div>
            <div className="bg-white rounded-xl p-6 border border-gray-200">
              <div className="text-3xl mb-4">🚨</div>
              <h3 className="font-semibold text-gray-900 mb-2">Deal Killers</h3>
              <p className="text-sm text-gray-600">Missing DPIIT recognition, informal ESOPs, FEMA red flags — these kill deals during investor diligence.</p>
            </div>
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section id="how-it-works" className="py-20">
        <div className="max-w-7xl mx-auto px-6">
          <h2 className="text-3xl font-bold text-center text-gray-900 mb-12">How It Works</h2>
          <div className="grid md:grid-cols-4 gap-8">
            {[
              { step: '1', icon: '📤', title: 'Upload Documents', desc: 'Drag & drop your cap table (CSV/XLSX), incorporation docs, board resolutions' },
              { step: '2', icon: '🔍', title: 'Auto-Parse & Extract', desc: 'Our parser extracts equity events, share classes, and ESOP grants automatically' },
              { step: '3', icon: '⚡', title: 'Run Compliance Checks', desc: '5 rules verify dilution math, DPIIT status, ESOP consistency, and more' },
              { step: '4', icon: '📊', title: 'Get Your Score', desc: 'Visual readiness score with priority actions and a shareable gap report' },
            ].map((item) => (
              <div key={item.step} className="text-center">
                <div className="w-12 h-12 bg-indigo-100 text-indigo-600 rounded-full flex items-center justify-center text-xl font-bold mx-auto mb-4">
                  {item.step}
                </div>
                <div className="text-3xl mb-2">{item.icon}</div>
                <h3 className="font-semibold text-gray-900 mb-2">{item.title}</h3>
                <p className="text-sm text-gray-600">{item.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="bg-gray-50 py-20">
        <div className="max-w-7xl mx-auto px-6">
          <h2 className="text-3xl font-bold text-center text-gray-900 mb-12">What We Check</h2>
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[
              { icon: '📊', title: 'Dilution Math', desc: 'Verifies total equity sums to exactly 100% across all funding rounds' },
              { icon: '🏛️', title: 'DPIIT Recognition', desc: 'Checks Section 56(2)(viib) angel tax exposure and recognition validity' },
              { icon: '👥', title: 'ESOP Consistency', desc: 'Cross-references informal promises against board-approved cap table' },
              { icon: '📋', title: 'Share Classes', desc: 'Validates share class consistency between incorporation docs and events' },
              { icon: '💰', title: 'Valuation Check', desc: 'Verifies price per share aligns with declared round valuation' },
              { icon: '📄', title: 'Gap Report', desc: 'LLM-powered narrative report with priority actions for founders' },
            ].map((f) => (
              <div key={f.title} className="bg-white rounded-xl p-6 border border-gray-200">
                <div className="text-3xl mb-3">{f.icon}</div>
                <h3 className="font-semibold text-gray-900 mb-2">{f.title}</h3>
                <p className="text-sm text-gray-600">{f.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Pricing */}
      <section className="py-20">
        <div className="max-w-7xl mx-auto px-6">
          <h2 className="text-3xl font-bold text-center text-gray-900 mb-12">Simple Pricing</h2>
          <div className="grid md:grid-cols-3 gap-8 max-w-4xl mx-auto">
            <div className="bg-white rounded-xl border border-gray-200 p-8">
              <h3 className="font-semibold text-gray-900 mb-2">Free</h3>
              <div className="text-4xl font-bold text-gray-900 mb-4">₹0</div>
              <ul className="space-y-3 text-sm text-gray-600 mb-8">
                <li>✓ 1 company</li>
                <li>✓ Basic compliance checks</li>
                <li>✓ Readiness score</li>
                <li className="text-gray-400">✗ PDF export</li>
                <li className="text-gray-400">✗ Email reports</li>
              </ul>
              <Link to="/register" className="block text-center py-2 border border-gray-300 rounded-lg text-sm font-medium hover:bg-gray-50">
                Get Started
              </Link>
              <Link to="/pricing" className="block text-center text-xs text-indigo-600 hover:text-indigo-500 mt-2">
                View all plans →
              </Link>
            </div>
            <div className="bg-indigo-600 rounded-xl p-8 text-white relative">
              <div className="absolute -top-3 left-1/2 -translate-x-1/2 px-3 py-1 bg-yellow-400 text-yellow-900 rounded-full text-xs font-bold">
                POPULAR
              </div>
              <h3 className="font-semibold mb-2">Pro</h3>
              <div className="text-4xl font-bold mb-4">₹999<span className="text-lg font-normal">/mo</span></div>
              <ul className="space-y-3 text-sm mb-8">
                <li>✓ Unlimited companies</li>
                <li>✓ Full compliance checks</li>
                <li>✓ PDF gap reports</li>
                <li>✓ Email notifications</li>
                <li>✓ Score history tracking</li>
              </ul>
              <Link to="/register" className="block text-center py-2 bg-white text-indigo-600 rounded-lg text-sm font-medium hover:bg-indigo-50">
                Start Pro Trial
              </Link>
            </div>
            <div className="bg-white rounded-xl border border-gray-200 p-8">
              <h3 className="font-semibold text-gray-900 mb-2">Advisor</h3>
              <div className="text-4xl font-bold text-gray-900 mb-4">₹4,999<span className="text-lg font-normal">/mo</span></div>
              <ul className="space-y-3 text-sm text-gray-600 mb-8">
                <li>✓ Everything in Pro</li>
                <li>✓ Multiple client companies</li>
                <li>✓ Bulk analysis</li>
                <li>✓ White-label reports</li>
                <li>✓ Priority support</li>
              </ul>
              <Link to="/register" className="block text-center py-2 border border-gray-300 rounded-lg text-sm font-medium hover:bg-gray-50">
                Contact Sales
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-gray-100 py-12">
        <div className="max-w-7xl mx-auto px-6 text-center">
          <div className="flex items-center justify-center gap-2 mb-4">
            <div className="w-6 h-6 bg-indigo-600 rounded flex items-center justify-center">
              <span className="text-white font-bold text-xs">FR</span>
            </div>
            <span className="font-semibold text-gray-900">FundraiseReady</span>
          </div>
          <p className="text-sm text-gray-500">
            Document-driven compliance diagnostic for Indian startups
          </p>
          <p className="text-xs text-gray-400 mt-4">
            © 2026 FundraiseReady. Built for DPIIT-registered startups.
          </p>
        </div>
      </footer>
    </div>
  )
}
