import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { useAuth } from '../hooks/useAuth'
import { stripeAPI } from '../lib/api'
import { CheckCircle2, ArrowRight, Zap } from 'lucide-react'

export default function Pricing() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(null)

  const handleCheckout = async (plan) => {
    if (!user) { navigate('/register'); return }
    setLoading(plan)
    try {
      const { data } = await stripeAPI.createCheckout(plan)
      window.location.href = data.url
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to start checkout. Make sure Stripe is configured.')
    } finally { setLoading(null) }
  }

  const plans = [
    { name: 'Free', price: '₹0', period: '', desc: 'Perfect for getting started', features: ['1 company', 'Basic compliance checks', 'Readiness score', 'Email notifications'], current: user?.plan === 'FREE', highlighted: false },
    { name: 'Pro', price: '₹999', period: '/mo', desc: 'For serious founders', features: ['Unlimited companies', 'Full compliance checks', 'PDF gap reports', 'Fix-it guidance', 'Score history', 'Priority support'], current: user?.plan === 'PRO', highlighted: true, planKey: 'pro' },
    { name: 'Advisor', price: '₹4,999', period: '/mo', desc: 'For advisory firms', features: ['Everything in Pro', 'Multiple client companies', 'Bulk analysis', 'White-label reports', 'Dedicated support', 'API access'], current: user?.plan === 'ADVISOR', highlighted: false, planKey: 'advisor' },
  ]

  const faqs = [
    { q: 'Can I cancel anytime?', a: "Yes, cancel anytime from your settings. You'll keep access until the end of your billing period." },
    { q: 'Is there a free trial for Pro?', a: 'Yes, Pro comes with a 14-day free trial. No credit card required to start.' },
    { q: 'What payment methods do you accept?', a: 'We accept all major credit/debit cards, UPI, and net banking through Stripe.' },
    { q: 'Do you offer refunds?', a: 'We offer a full refund within 7 days of any paid subscription charge.' },
  ]

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-indigo-50/30">
      {/* Nav */}
      <motion.nav initial={{ y: -20, opacity: 0 }} animate={{ y: 0, opacity: 1 }}
        className="glass sticky top-0 z-50 border-b border-white/20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-4 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl flex items-center justify-center shadow-lg shadow-indigo-500/25"
                 style={{ background: 'var(--gradient-primary)' }}>
              <span className="text-white font-bold text-sm">FR</span>
            </div>
            <span className="text-lg font-bold text-gray-900">Fundraise<span className="text-indigo-600">Ready</span></span>
          </Link>
          <div className="flex items-center gap-3">
            {user ? (
              <Link to="/app" className="text-sm text-gray-600 hover:text-gray-900 font-medium">Dashboard</Link>
            ) : (
              <>
                <Link to="/login" className="text-sm text-gray-600 hover:text-gray-900 font-medium">Login</Link>
                <Link to="/register" className="btn-primary flex items-center gap-1 text-sm">Get Started</Link>
              </>
            )}
          </div>
        </div>
      </motion.nav>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-20">
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="text-center mb-16">
          <span className="text-sm font-semibold text-indigo-600 tracking-wider uppercase">Pricing</span>
          <h1 className="text-4xl sm:text-5xl font-bold text-gray-900 mt-3 mb-4">Simple, transparent pricing</h1>
          <p className="text-xl text-gray-500 max-w-2xl mx-auto">Start free, upgrade when you're ready. No hidden fees.</p>
        </motion.div>

        <div className="grid md:grid-cols-3 gap-8 max-w-5xl mx-auto">
          {plans.map((plan, i) => (
            <motion.div
              key={plan.name}
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.1 }}
              whileHover={{ y: -8 }}
              className={`rounded-2xl p-8 relative ${
                plan.highlighted
                  ? 'bg-gradient-to-br from-indigo-600 via-purple-600 to-indigo-700 text-white ring-4 ring-indigo-500 shadow-2xl shadow-indigo-500/25'
                  : 'glass-card'
              }`}
            >
              {plan.highlighted && (
                <div className="absolute -top-3 left-1/2 -translate-x-1/2 px-4 py-1 bg-gradient-to-r from-yellow-400 to-orange-400 text-white rounded-full text-xs font-bold shadow-lg flex items-center gap-1">
                  <Zap className="w-3 h-3" /> MOST POPULAR
                </div>
              )}
              <h3 className={`font-semibold text-lg mb-2 ${plan.highlighted ? '' : 'text-gray-900'}`}>{plan.name}</h3>
              <div className="flex items-baseline gap-1 mb-2">
                <span className={`text-5xl font-bold ${plan.highlighted ? '' : 'text-gray-900'}`}>{plan.price}</span>
                {plan.period && <span className={`text-lg ${plan.highlighted ? 'text-indigo-200' : 'text-gray-500'}`}>{plan.period}</span>}
              </div>
              <p className={`text-sm mb-8 ${plan.highlighted ? 'text-indigo-200' : 'text-gray-500'}`}>{plan.desc}</p>
              <ul className="space-y-3 mb-8">
                {plan.features.map((f) => (
                  <li key={f} className="flex items-center gap-2.5 text-sm">
                    <CheckCircle2 className={`w-4 h-4 flex-shrink-0 ${plan.highlighted ? 'text-indigo-200' : 'text-green-500'}`} />
                    {f}
                  </li>
                ))}
              </ul>
              <motion.button
                whileHover={{ scale: 1.03 }}
                whileTap={{ scale: 0.97 }}
                onClick={() => plan.planKey && handleCheckout(plan.planKey)}
                disabled={plan.current || loading === plan.planKey}
                className={`w-full py-3.5 rounded-xl font-semibold transition-all duration-300 ${
                  plan.current
                    ? 'bg-white/10 text-white/50 cursor-default'
                    : plan.highlighted
                      ? 'bg-white text-indigo-600 hover:bg-indigo-50 shadow-lg'
                      : 'bg-gray-900 text-white hover:bg-gray-800'
                } disabled:opacity-50`}
              >
                {loading === plan.planKey ? 'Redirecting...' : plan.current ? 'Current Plan' : 'Get Started'}
              </motion.button>
            </motion.div>
          ))}
        </div>

        {/* FAQ */}
        <div className="mt-24 max-w-3xl mx-auto">
          <motion.h2 initial={{ opacity: 0 }} whileInView={{ opacity: 1 }} viewport={{ once: true }}
            className="text-3xl font-bold text-gray-900 text-center mb-10">Frequently Asked Questions</motion.h2>
          <div className="space-y-4">
            {faqs.map((faq, i) => (
              <motion.div
                key={faq.q}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ delay: i * 0.1 }}
                className="glass-card p-6"
              >
                <h3 className="font-semibold text-gray-900 mb-2">{faq.q}</h3>
                <p className="text-sm text-gray-600 leading-relaxed">{faq.a}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </div>

      <footer className="border-t border-gray-100 py-8 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 text-center">
          <p className="text-sm text-gray-500">© 2026 FundraiseReady. Built for DPIIT-registered startups.</p>
        </div>
      </footer>
    </div>
  )
}
