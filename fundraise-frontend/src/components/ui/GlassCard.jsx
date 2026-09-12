import { motion } from 'framer-motion'

export default function GlassCard({ children, className = '', hover = true, ...props }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, ease: [0.4, 0, 0.2, 1] }}
      whileHover={hover ? { y: -4, boxShadow: '0 20px 40px rgba(31, 38, 135, 0.2)' } : {}}
      className={`glass-card ${className}`}
      {...props}
    >
      {children}
    </motion.div>
  )
}
