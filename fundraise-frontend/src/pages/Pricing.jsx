import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { stripeAPI } from '../lib/api'

export default function Pricing() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(null)

  const handleCheckout = async (plan) => {
    if (!user) {
      navigate('/register')
      return
    }

    setLoading(plan)
    try {
      const { data } = await stripeAPI.createCheckout(plan)
      window.location.href = data.url
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to start checkout. Make sure Stripe is configured.')
    } finally {
      setLoading(null)
    }
  }

  const plans = [
    {
      name: 'Free',
      price: '₹0',
      period: '',
      description: 'Perfect for getting started',
      features: [
        '1 company',
        'Basic compliance checks',
        'Readiness score',
        'Email notifications',
      ],
      excluded: [
        'PDF export',
        'Score history tracking',
        'Priority support',
      ],
      cta: user?.plan === 'FREE' ? 'Current Plan' : 'Get Started',
      current: user?.plan === 'FREE',
      highlighted: false,
    },
    {
      name: 'Pro',
      price: '₹999',
      period: '/mo',
      description: 'For serious founders',
      features: [
        'Unlimited companies',
        'Full compliance checks',
        'PDF gap reports',
        'Email notifications',
        'Score history tracking',
        'Priority support',
      ],
      excluded: [],
      cta: user?.plan === 'PRO' ? 'Current Plan' : 'Start Pro Trial',
      current: user?.plan === 'PRO',
      highlighted: true,
      planKey: 'pro',
    },
    {
      name: 'Advisor',
      price: '₹4,999',
      period: '/mo',
      description: 'For advisory firms',
      features: [
        'Everything in Pro',
        'Multiple client companies',
        'Bulk analysis',
        'White-label reports',
        'Dedicated support',
        'API access',
      ],
      excluded: [],
      cta: user?.plan === 'ADVISOR' ? 'Current Plan' : 'Contact Sales',
      current: user?.plan === 'ADVISOR',
      highlighted: false,
      planKey: 'advisor',
    },
  ]

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Nav */}
      <nav className="bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2">
            <div className="w-8 h-8 bg-indigo-600 rounded-lg flex items-center justify-center">
              <span className="text-white font-bold text-sm">FR</span>
            </div>
            <span className="text-xl font-bold text-gray-900">FundraiseReady</span>
          </Link>
          <div className="flex items-center gap-4">
            {user ? (
              <Link to="/app" className="text-sm text-gray-600 hover:text-gray-900">
                Dashboard
              </Link>
            ) : (
              <>
                <Link to="/login" className="text-sm text-gray-600 hover:text-gray-900">Login</Link>
                <Link to="/register" className="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700">
                  Get Started
                </Link>
              </>
            )}
          </div>
        </div>
      </nav>

      {/* Pricing */}
      <div className="max-w-7xl mx-auto px-6 py-20">
        <div className="text-center mb-16">
          <h1 className="text-4xl font-bold text-gray-900 mb-4">Simple, transparent pricing</h1>
          <p className="text-xl text-gray-600 max-w-2xl mx-auto">
            Start free, upgrade when you're ready. No hidden fees.
          </p>
        </div>

        <div className="grid md:grid-cols-3 gap-8 max-w-5xl mx-auto">
          {plans.map((plan) => (
            <div
              key={plan.name}
              className={`rounded-xl p-8 relative ${
                plan.highlighted
                  ? 'bg-indigo-600 text-white ring-4 ring-indigo-600'
                  : 'bg-white border border-gray-200'
              }`}
            >
              {plan.highlighted && (
                <div className="absolute -top-3 left-1/2 -translate-x-1/2 px-3 py-1 bg-yellow-400 text-yellow-900 rounded-full text-xs font-bold">
                  MOST POPULAR
                </div>
              )}

              <h3 className={`font-semibold text-lg mb-2 ${plan.highlighted ? '' : 'text-gray-900'}`}>
                {plan.name}
              </h3>
              <div className="flex items-baseline gap-1 mb-2">
                <span className={`text-4xl font-bold ${plan.highlighted ? '' : 'text-gray-900'}`}>
                  {plan.price}
                </span>
                {plan.period && (
                  <span className={`text-lg ${plan.highlighted ? 'text-indigo-200' : 'text-gray-500'}`}>
                    {plan.period}
                  </span>
                )}
              </div>
              <p className={`text-sm mb-6 ${plan.highlighted ? 'text-indigo-200' : 'text-gray-500'}`}>
                {plan.description}
              </p>

              <ul className="space-y-3 mb-8">
                {plan.features.map((f) => (
                  <li key={f} className="flex items-center gap-2 text-sm">
                    <svg className={`w-4 h-4 flex-shrink-0 ${plan.highlighted ? 'text-indigo-200' : 'text-green-500'}`} fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                    </svg>
                    {f}
                  </li>
                ))}
                {plan.excluded.map((f) => (
                  <li key={f} className="flex items-center gap-2 text-sm opacity-50">
                    <svg className={`w-4 h-4 flex-shrink-0 ${plan.highlighted ? 'text-indigo-300' : 'text-gray-400'}`} fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
                    </svg>
                    {f}
                  </li>
                ))}
              </ul>

              <button
                onClick={() => plan.planKey && handleCheckout(plan.planKey)}
                disabled={plan.current || loading === plan.planKey}
                className={`w-full py-3 rounded-lg text-sm font-medium transition-all ${
                  plan.current
                    ? plan.highlighted
                      ? 'bg-indigo-500 text-white cursor-default'
                      : 'bg-gray-100 text-gray-500 cursor-default'
                    : plan.highlighted
                      ? 'bg-white text-indigo-600 hover:bg-indigo-50'
                      : 'bg-indigo-600 text-white hover:bg-indigo-700'
                } disabled:opacity-50`}
              >
                {loading === plan.planKey ? 'Redirecting...' : plan.cta}
              </button>
            </div>
          ))}
        </div>

        {/* FAQ */}
        <div className="mt-20 max-w-3xl mx-auto">
          <h2 className="text-2xl font-bold text-gray-900 text-center mb-10">Frequently Asked Questions</h2>
          <div className="space-y-6">
            {[
              {
                q: 'Can I cancel anytime?',
                a: 'Yes, cancel anytime from your settings. You\'ll keep access until the end of your billing period.',
              },
              {
                q: 'Is there a free trial for Pro?',
                a: 'Yes, Pro comes with a 14-day free trial. No credit card required to start.',
              },
              {
                q: 'What payment methods do you accept?',
                a: 'We accept all major credit/debit cards, UPI, and net banking through Stripe.',
              },
              {
                q: 'Do you offer refunds?',
                a: 'We offer a full refund within 7 days of any paid subscription charge.',
              },
            ].map((faq) => (
              <div key={faq.q} className="bg-white rounded-xl border border-gray-200 p-6">
                <h3 className="font-semibold text-gray-900 mb-2">{faq.q}</h3>
                <p className="text-sm text-gray-600">{faq.a}</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Footer */}
      <footer className="border-t border-gray-200 py-8 bg-white">
        <div className="max-w-7xl mx-auto px-6 text-center">
          <p className="text-sm text-gray-500">
            © 2026 FundraiseReady. Built for DPIIT-registered startups.
          </p>
        </div>
      </footer>
    </div>
  )
}
