import { useEffect, useState } from 'react'
import { motion, useSpring, useTransform } from 'framer-motion'

export default function AnimatedCounter({ value, duration = 1, className = '' }) {
  const spring = useSpring(0, { duration: duration * 1000, bounce: 0 })
  const display = useTransform(spring, (current) => Math.round(current))
  const [displayValue, setDisplayValue] = useState(0)

  useEffect(() => {
    spring.set(value)
    const unsubscribe = display.on('change', (v) => setDisplayValue(v))
    return unsubscribe
  }, [value, spring, display])

  return (
    <motion.span
      initial={{ scale: 0.5, opacity: 0 }}
      animate={{ scale: 1, opacity: 1 }}
      transition={{ type: 'spring', stiffness: 200, damping: 10 }}
      className={className}
    >
      {displayValue}
    </motion.span>
  )
}
