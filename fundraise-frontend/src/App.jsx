import { Routes, Route, Navigate, useLocation } from 'react-router-dom'
import { AnimatePresence } from 'framer-motion'
import { Toaster } from 'react-hot-toast'
import { useAuth } from './hooks/useAuth'
import Landing from './pages/Landing'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import Upload from './pages/Upload'
import GapReport from './pages/GapReport'
import VerifyEmail from './pages/VerifyEmail'
import ForgotPassword from './pages/ForgotPassword'
import ResetPassword from './pages/ResetPassword'
import Settings from './pages/Settings'
import Pricing from './pages/Pricing'
import Layout from './components/Layout'
import PageTransition from './components/ui/PageTransition'

function ProtectedRoute({ children }) {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  return children
}

function PublicRoute({ children }) {
  const { user } = useAuth()
  if (user) return <Navigate to="/app" replace />
  return children
}

function App() {
  const location = useLocation()

  return (
    <>
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 3000,
          style: {
            background: 'rgba(255, 255, 255, 0.9)',
            backdropFilter: 'blur(20px)',
            border: '1px solid rgba(255, 255, 255, 0.3)',
            borderRadius: '12px',
            boxShadow: '0 8px 32px rgba(31, 38, 135, 0.15)',
          },
        }}
      />
      <AnimatePresence mode="wait">
        <Routes location={location} key={location.pathname}>
          <Route path="/" element={<PageTransition><Landing /></PageTransition>} />
          <Route path="/login" element={<PublicRoute><PageTransition><Login /></PageTransition></PublicRoute>} />
          <Route path="/register" element={<PublicRoute><PageTransition><Register /></PageTransition></PublicRoute>} />
          <Route path="/forgot-password" element={<PublicRoute><PageTransition><ForgotPassword /></PageTransition></PublicRoute>} />
          <Route path="/reset-password" element={<PageTransition><ResetPassword /></PageTransition>} />
          <Route path="/verify-email" element={<PageTransition><VerifyEmail /></PageTransition>} />
          <Route path="/pricing" element={<PageTransition><Pricing /></PageTransition>} />
          <Route path="/app" element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }>
            <Route index element={<PageTransition><Dashboard /></PageTransition>} />
            <Route path="upload/:companyId" element={<PageTransition><Upload /></PageTransition>} />
            <Route path="report/:companyId" element={<PageTransition><GapReport /></PageTransition>} />
            <Route path="settings" element={<PageTransition><Settings /></PageTransition>} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AnimatePresence>
    </>
  )
}

export default App
