import { Link } from 'react-router-dom'
import { motion, useScroll, useTransform } from 'framer-motion'
import { useRef } from 'react'
import { ArrowRight, Shield, FileSearch, BarChart3, Zap, CheckCircle2, AlertTriangle, XCircle, ChevronRight, Star } from 'lucide-react'

const fadeUp = {
  initial: { opacity: 0, y: 30 },
  whileInView: { opacity: 1, y: 0 },
  viewport: { once: true, margin: '-100px' },
  transition: { duration: 0.6, ease: [0.4, 0, 0.2, 1] },
}

const stagger = {
  animate: { transition: { staggerChildren: 0.1 } },
}

export default function Landing() {
  const heroRef = useRef(null)
  const { scrollYProgress } = useScroll({ target: heroRef, offset: ['start start', 'end start'] })
  const heroY = useTransform(scrollYProgress, [0, 1], [0, 150])
  const heroOpacity = useTransform(scrollYProgress, [0, 0.8], [1, 0])

  return (
    <div className="min-h-screen bg-white overflow-hidden">
      {/* ============================================
          NAVIGATION
          ============================================ */}
      <motion.nav
        initial={{ y: -20, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.5 }}
        className="glass sticky top-0 z-50 border-b border-white/20"
      >
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-4 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl flex items-center justify-center shadow-lg shadow-indigo-500/25"
                 style={{ background: 'var(--gradient-primary)' }}>
              <span className="text-white font-bold text-sm">FR</span>
            </div>
            <span className="text-lg font-bold text-gray-900">
              Fundraise<span className="text-indigo-600">Ready</span>
            </span>
          </Link>
          <div className="flex items-center gap-3">
            <Link to="/login" className="text-sm text-gray-600 hover:text-gray-900 font-medium transition-colors hidden sm:block">
              Login
            </Link>
            <Link to="/register"
              className="btn-primary flex items-center gap-2 text-sm">
              Get Started Free <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
        </div>
      </motion.nav>

      {/* ============================================
          HERO SECTION
          ============================================ */}
      <section ref={heroRef} className="hero-gradient relative min-h-[90vh] flex items-center">
        {/* Floating orbs */}
        <div className="orb orb-1" />
        <div className="orb orb-2" />
        <div className="orb orb-3" />

        <motion.div style={{ y: heroY, opacity: heroOpacity }} className="max-w-7xl mx-auto px-4 sm:px-6 py-20 relative z-10">
          <div className="text-center max-w-4xl mx-auto">
            <motion.div
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.5 }}
              className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-white/10 border border-white/20 text-white/80 text-sm font-medium mb-8 backdrop-blur-sm"
            >
              <Zap className="w-4 h-4 text-yellow-400" />
              For Indian Startups Preparing to Fundraise
            </motion.div>

            <motion.h1
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.1 }}
              className="text-5xl sm:text-7xl font-bold text-white mb-6 leading-tight"
            >
              Know Your Fundraise
              <br />
              <span className="bg-gradient-to-r from-indigo-400 via-purple-400 to-cyan-400 bg-clip-text text-transparent">
                Readiness Score
              </span>
            </motion.h1>

            <motion.p
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.2 }}
              className="text-lg sm:text-xl text-white/70 mb-10 max-w-2xl mx-auto leading-relaxed"
            >
              Upload your cap table and compliance documents. Get instant analysis against
              DPIIT, FEMA, ESOP, and valuation rules that investors actually check.
            </motion.p>

            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.3 }}
              className="flex flex-col sm:flex-row items-center justify-center gap-4"
            >
              <Link to="/register"
                className="group px-8 py-4 bg-white text-indigo-700 rounded-xl font-semibold hover:bg-indigo-50 transition-all duration-300 shadow-xl shadow-black/10 flex items-center gap-2 text-lg">
                Start Free
                <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
              </Link>
              <a href="#how-it-works"
                className="px-8 py-4 border border-white/20 text-white rounded-xl font-medium hover:bg-white/10 transition-all duration-300 backdrop-blur-sm">
                How It Works
              </a>
            </motion.div>

            <motion.p
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.5 }}
              className="text-sm text-white/40 mt-6"
            >
              No credit card required • 1 company free tier
            </motion.p>
          </div>
        </motion.div>

        {/* Gradient fade at bottom */}
        <div className="absolute bottom-0 left-0 right-0 h-32 bg-gradient-to-t from-white to-transparent" />
      </section>

      {/* ============================================
          PROBLEM SECTION
          ============================================ */}
      <section className="py-24 relative">
        <div className="max-w-7xl mx-auto px-4 sm:px-6">
          <motion.div {...fadeUp} className="text-center mb-16">
            <span className="text-sm font-semibold text-indigo-600 tracking-wider uppercase">The Problem</span>
            <h2 className="text-4xl font-bold text-gray-900 mt-3 mb-4">
              Fundraise readiness is broken
            </h2>
            <p className="text-gray-500 text-lg max-w-2xl mx-auto">
              Founders lose months and lakhs to manual compliance checks that could be automated.
            </p>
          </motion.div>

          <motion.div variants={stagger} initial="initial" whileInView="animate" viewport={{ once: true }}
            className="grid md:grid-cols-3 gap-6">
            {[
              { icon: AlertTriangle, title: 'Manual Compliance Checks', desc: 'Advisory firms charge ₹50,000-2,00,000 to manually verify your documents against regulatory patterns.', color: 'from-red-500 to-orange-500', bg: 'bg-red-50' },
              { icon: XCircle, title: 'Spreadsheet Drift', desc: 'Cap tables get out of sync. ESOP promises are forgotten. Dilution math doesn\'t add up to 100%.', color: 'from-amber-500 to-yellow-500', bg: 'bg-amber-50' },
              { icon: Shield, title: 'Deal Killers', desc: 'Missing DPIIT recognition, informal ESOPs, FEMA red flags — these kill deals during investor diligence.', color: 'from-rose-500 to-pink-500', bg: 'bg-rose-50' },
            ].map((item) => (
              <motion.div
                key={item.title}
                variants={fadeUp}
                whileHover={{ y: -4 }}
                className="glass-card p-8 group cursor-default"
              >
                <div className={`w-12 h-12 rounded-xl ${item.bg} flex items-center justify-center mb-5 group-hover:scale-110 transition-transform`}>
                  <item.icon className={`w-6 h-6 bg-gradient-to-r ${item.color} bg-clip-text`} style={{ color: item.color.includes('red') ? '#ef4444' : item.color.includes('amber') ? '#f59e0b' : '#f43f5e' }} />
                </div>
                <h3 className="text-lg font-semibold text-gray-900 mb-2">{item.title}</h3>
                <p className="text-gray-500 text-sm leading-relaxed">{item.desc}</p>
              </motion.div>
            ))}
          </motion.div>
        </div>
      </section>

      {/* ============================================
          HOW IT WORKS
          ============================================ */}
      <section id="how-it-works" className="py-24 bg-gradient-to-b from-gray-50 to-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6">
          <motion.div {...fadeUp} className="text-center mb-16">
            <span className="text-sm font-semibold text-indigo-600 tracking-wider uppercase">How It Works</span>
            <h2 className="text-4xl font-bold text-gray-900 mt-3 mb-4">
              Four steps to fundraise readiness
            </h2>
          </motion.div>

          <div className="grid md:grid-cols-4 gap-8">
            {[
              { step: '1', icon: '📤', title: 'Upload Documents', desc: 'Drag & drop your cap table (CSV/XLSX), incorporation docs, board resolutions' },
              { step: '2', icon: '🔍', title: 'Auto-Parse & Extract', desc: 'Our parser extracts equity events, share classes, and ESOP grants automatically' },
              { step: '3', icon: '⚡', title: 'Run Compliance Checks', desc: '5 rules verify dilution math, DPIIT status, ESOP consistency, and more' },
              { step: '4', icon: '📊', title: 'Get Your Score', desc: 'Visual readiness score with priority actions and a shareable gap report' },
            ].map((item, i) => (
              <motion.div
                key={item.step}
                initial={{ opacity: 0, y: 30 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ delay: i * 0.1, duration: 0.5 }}
                className="text-center relative"
              >
                {i < 3 && (
                  <div className="hidden md:block absolute top-10 left-[60%] w-[80%] h-[2px] bg-gradient-to-r from-indigo-200 to-transparent" />
                )}
                <div className="w-14 h-14 rounded-2xl flex items-center justify-center text-2xl font-bold mx-auto mb-5 bg-gradient-to-br from-indigo-500 to-purple-600 text-white shadow-lg shadow-indigo-500/25">
                  {item.step}
                </div>
                <div className="text-3xl mb-3">{item.icon}</div>
                <h3 className="font-semibold text-gray-900 mb-2">{item.title}</h3>
                <p className="text-sm text-gray-500 leading-relaxed">{item.desc}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* ============================================
          FEATURES / WHAT WE CHECK
          ============================================ */}
      <section className="py-24">
        <div className="max-w-7xl mx-auto px-4 sm:px-6">
          <motion.div {...fadeUp} className="text-center mb-16">
            <span className="text-sm font-semibold text-indigo-600 tracking-wider uppercase">What We Check</span>
            <h2 className="text-4xl font-bold text-gray-900 mt-3 mb-4">
              Five rules that matter
            </h2>
            <p className="text-gray-500 text-lg max-w-2xl mx-auto">
              Each rule encodes a specific failure pattern that kills deals during investor diligence.
            </p>
          </motion.div>

          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[
              { icon: BarChart3, title: 'Dilution Math', desc: 'Verifies total equity sums to exactly 100% across all funding rounds', color: 'indigo' },
              { icon: Shield, title: 'DPIIT Recognition', desc: 'Checks Section 56(2)(viib) angel tax exposure and recognition validity', color: 'purple' },
              { icon: CheckCircle2, title: 'ESOP Consistency', desc: 'Cross-references informal promises against board-approved cap table', color: 'cyan' },
              { icon: FileSearch, title: 'Share Classes', desc: 'Validates share class consistency between incorporation docs and events', color: 'emerald' },
              { icon: Zap, title: 'Valuation Check', desc: 'Verifies price per share aligns with declared round valuation', color: 'amber' },
              { icon: Star, title: 'Fix-It Guidance', desc: 'Step-by-step instructions for fixing each finding, with effort estimates', color: 'rose' },
            ].map((f, i) => (
              <motion.div
                key={f.title}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ delay: i * 0.08 }}
                whileHover={{ y: -4, boxShadow: '0 20px 40px rgba(0,0,0,0.1)' }}
                className="glass-card p-6 group"
              >
                <div className={`w-11 h-11 rounded-xl bg-${f.color}-50 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform`}>
                  <f.icon className={`w-5 h-5 text-${f.color}-600`} />
                </div>
                <h3 className="font-semibold text-gray-900 mb-2">{f.title}</h3>
                <p className="text-sm text-gray-500 leading-relaxed">{f.desc}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* ============================================
          PRICING PREVIEW
          ============================================ */}
      <section className="py-24 bg-gradient-to-b from-gray-50 to-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6">
          <motion.div {...fadeUp} className="text-center mb-16">
            <span className="text-sm font-semibold text-indigo-600 tracking-wider uppercase">Pricing</span>
            <h2 className="text-4xl font-bold text-gray-900 mt-3 mb-4">Simple, transparent pricing</h2>
          </motion.div>

          <div className="grid md:grid-cols-3 gap-8 max-w-5xl mx-auto">
            {[
              { name: 'Free', price: '₹0', period: '', features: ['1 company', 'Basic compliance checks', 'Readiness score', 'Email notifications'], cta: 'Get Started', highlighted: false },
              { name: 'Pro', price: '₹999', period: '/mo', features: ['Unlimited companies', 'Full compliance checks', 'PDF gap reports', 'Fix-it guidance', 'Score history'], cta: 'Start Pro Trial', highlighted: true },
              { name: 'Advisor', price: '₹4,999', period: '/mo', features: ['Everything in Pro', 'Multiple client companies', 'Bulk analysis', 'White-label reports', 'Priority support'], cta: 'Contact Sales', highlighted: false },
            ].map((plan) => (
              <motion.div
                key={plan.name}
                whileHover={{ y: -4 }}
                className={`rounded-2xl p-8 relative ${plan.highlighted ? 'bg-gradient-to-br from-indigo-600 to-purple-700 text-white ring-4 ring-indigo-600 shadow-xl shadow-indigo-500/25' : 'glass-card'}`}
              >
                {plan.highlighted && (
                  <div className="absolute -top-3 left-1/2 -translate-x-1/2 px-3 py-1 bg-gradient-to-r from-yellow-400 to-orange-400 text-white rounded-full text-xs font-bold shadow-lg">
                    MOST POPULAR
                  </div>
                )}
                <h3 className={`font-semibold text-lg mb-2 ${plan.highlighted ? '' : 'text-gray-900'}`}>{plan.name}</h3>
                <div className="flex items-baseline gap-1 mb-2">
                  <span className={`text-4xl font-bold ${plan.highlighted ? '' : 'text-gray-900'}`}>{plan.price}</span>
                  {plan.period && <span className={`text-lg ${plan.highlighted ? 'text-indigo-200' : 'text-gray-500'}`}>{plan.period}</span>}
                </div>
                <ul className="space-y-3 my-8">
                  {plan.features.map((f) => (
                    <li key={f} className="flex items-center gap-2 text-sm">
                      <CheckCircle2 className={`w-4 h-4 flex-shrink-0 ${plan.highlighted ? 'text-indigo-200' : 'text-green-500'}`} />
                      {f}
                    </li>
                  ))}
                </ul>
                <Link to="/register"
                  className={`block text-center py-3 rounded-xl font-semibold transition-all duration-300 ${
                    plan.highlighted
                      ? 'bg-white text-indigo-600 hover:bg-indigo-50 shadow-lg'
                      : 'bg-gray-900 text-white hover:bg-gray-800'
                  }`}>
                  {plan.cta}
                </Link>
              </motion.div>
            ))}
          </div>

          <motion.div {...fadeUp} className="text-center mt-8">
            <Link to="/pricing" className="text-indigo-600 hover:text-indigo-700 font-medium text-sm inline-flex items-center gap-1">
              View all plans and FAQ <ChevronRight className="w-4 h-4" />
            </Link>
          </motion.div>
        </div>
      </section>

      {/* ============================================
          CTA SECTION
          ============================================ */}
      <section className="py-24">
        <div className="max-w-7xl mx-auto px-4 sm:px-6">
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            whileInView={{ opacity: 1, scale: 1 }}
            viewport={{ once: true }}
            className="relative rounded-3xl overflow-hidden p-12 sm:p-16 text-center"
            style={{ background: 'var(--gradient-hero)' }}
          >
            <div className="orb orb-1 opacity-20" />
            <div className="orb orb-2 opacity-20" />
            <div className="relative z-10">
              <h2 className="text-3xl sm:text-4xl font-bold text-white mb-4">
                Ready to know your fundraise readiness?
              </h2>
              <p className="text-white/70 text-lg mb-8 max-w-xl mx-auto">
                Join hundreds of Indian startups using automated compliance checks to prepare for fundraise.
              </p>
              <Link to="/register"
                className="inline-flex items-center gap-2 px-8 py-4 bg-white text-indigo-700 rounded-xl font-semibold hover:bg-indigo-50 transition-all shadow-xl text-lg">
                Start Free <ArrowRight className="w-5 h-5" />
              </Link>
            </div>
          </motion.div>
        </div>
      </section>

      {/* ============================================
          FOOTER
          ============================================ */}
      <footer className="border-t border-gray-100 py-12 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 text-center">
          <div className="flex items-center justify-center gap-2 mb-4">
            <div className="w-7 h-7 rounded-lg flex items-center justify-center"
                 style={{ background: 'var(--gradient-primary)' }}>
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
