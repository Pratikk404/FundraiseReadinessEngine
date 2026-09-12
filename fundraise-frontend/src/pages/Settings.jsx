import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { motion } from 'framer-motion'
import { profileAPI } from '../lib/api'
import { useAuth } from '../hooks/useAuth'
import { User, Lock, CreditCard, CheckCircle2, AlertCircle, ArrowRight } from 'lucide-react'

export default function Settings() {
  const [searchParams] = useSearchParams()
  const queryClient = useQueryClient()
  const { login, user } = useAuth()
  const upgraded = searchParams.get('upgraded') === 'true'

  const [name, setName] = useState(user?.name || '')
  const [email, setEmail] = useState(user?.email || '')
  const [profileMsg, setProfileMsg] = useState('')
  const [profileErr, setProfileErr] = useState('')

  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [passwordMsg, setPasswordMsg] = useState('')
  const [passwordErr, setPasswordErr] = useState('')

  const { data: profile, isLoading } = useQuery({
    queryKey: ['profile'],
    queryFn: () => profileAPI.getProfile().then(r => r.data),
  })

  const updateProfileMutation = useMutation({
    mutationFn: (data) => profileAPI.updateProfile(data),
    onSuccess: (res) => {
      setProfileMsg('Profile updated successfully')
      setProfileErr('')
      login(localStorage.getItem('token'), { ...user, name: res.data.name, email: res.data.email })
      queryClient.invalidateQueries({ queryKey: ['profile'] })
    },
    onError: (err) => { setProfileErr(err.response?.data?.message || 'Failed to update'); setProfileMsg('') },
  })

  const changePasswordMutation = useMutation({
    mutationFn: (data) => profileAPI.changePassword(data),
    onSuccess: () => {
      setPasswordMsg('Password changed successfully')
      setPasswordErr('')
      setCurrentPassword(''); setNewPassword(''); setConfirmPassword('')
    },
    onError: (err) => { setPasswordErr(err.response?.data?.message || 'Failed to change'); setPasswordMsg('') },
  })

  const handleProfileSubmit = (e) => {
    e.preventDefault(); setProfileMsg(''); setProfileErr('')
    updateProfileMutation.mutate({ name, email })
  }

  const handlePasswordSubmit = (e) => {
    e.preventDefault(); setPasswordMsg(''); setPasswordErr('')
    if (newPassword !== confirmPassword) { setPasswordErr('Passwords do not match'); return }
    changePasswordMutation.mutate({ currentPassword, newPassword })
  }

  const planConfig = {
    FREE: { bg: 'bg-gray-100', text: 'text-gray-700', label: 'Free' },
    PRO: { bg: 'bg-indigo-500/10', text: 'text-indigo-500', label: 'Pro' },
    ADVISOR: { bg: 'bg-purple-500/10', text: 'text-purple-500', label: 'Advisor' },
  }

  const plan = planConfig[profile?.plan] || planConfig.FREE

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="w-10 h-10 border-3 border-indigo-200 border-t-indigo-600 rounded-full animate-spin" />
      </div>
    )
  }

  return (
    <div>
      <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }}>
        <h1 className="text-2xl font-bold text-gray-900">Settings</h1>
        <p className="text-sm text-gray-500 mt-1">Manage your account settings and preferences</p>
      </motion.div>

      {upgraded && (
        <motion.div
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
          className="mt-4 glass-card p-4 border-l-4 border-green-500 bg-green-50/50"
        >
          <p className="text-sm text-green-700 font-medium flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4" /> 🎉 Your plan has been upgraded! Changes are now active.
          </p>
        </motion.div>
      )}

      <div className="space-y-6 mt-6">
        {/* Plan Card */}
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.1 }} className="glass-card p-6">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center">
              <CreditCard className="w-5 h-5 text-white" />
            </div>
            <h2 className="text-lg font-semibold text-gray-900">Current Plan</h2>
          </div>
          <div className="flex items-center gap-4">
            <span className={`text-sm px-3 py-1 rounded-full font-semibold ${plan.bg} ${plan.text}`}>{plan.label}</span>
            <span className="text-sm text-gray-500">
              {profile?.plan === 'FREE' ? '1 company, basic checks' : 'Unlimited companies, full features'}
            </span>
            {profile?.plan === 'FREE' && (
              <a href="/pricing" className="text-sm text-indigo-600 hover:text-indigo-700 font-semibold flex items-center gap-1 ml-auto">
                Upgrade <ArrowRight className="w-3 h-3" />
              </a>
            )}
          </div>
        </motion.div>

        {/* Profile Form */}
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }} className="glass-card p-6">
          <div className="flex items-center gap-3 mb-5">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 to-cyan-600 flex items-center justify-center">
              <User className="w-5 h-5 text-white" />
            </div>
            <h2 className="text-lg font-semibold text-gray-900">Profile</h2>
          </div>
          <form onSubmit={handleProfileSubmit} className="space-y-4 max-w-md">
            {profileMsg && (
              <motion.div initial={{ opacity: 0, y: -5 }} animate={{ opacity: 1, y: 0 }}
                className="bg-green-50 border border-green-200 text-green-600 px-4 py-3 rounded-xl text-sm flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4" /> {profileMsg}
              </motion.div>
            )}
            {profileErr && (
              <motion.div initial={{ opacity: 0, y: -5 }} animate={{ opacity: 1, y: 0 }}
                className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-xl text-sm flex items-center gap-2">
                <AlertCircle className="w-4 h-4" /> {profileErr}
              </motion.div>
            )}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Full Name</label>
              <input type="text" value={name} onChange={(e) => setName(e.target.value)} className="glass-input w-full" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} className="glass-input w-full" />
            </div>
            <div className="flex items-center gap-2 text-sm">
              {profile?.verified ? (
                <span className="text-green-600 flex items-center gap-1"><CheckCircle2 className="w-4 h-4" /> Email verified</span>
              ) : (
                <span className="text-amber-600">Email not verified</span>
              )}
            </div>
            <motion.button whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}
              type="submit" disabled={updateProfileMutation.isPending}
              className="btn-primary disabled:opacity-50">
              {updateProfileMutation.isPending ? 'Saving...' : 'Save Changes'}
            </motion.button>
          </form>
        </motion.div>

        {/* Password Form */}
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.3 }} className="glass-card p-6">
          <div className="flex items-center gap-3 mb-5">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-rose-500 to-pink-600 flex items-center justify-center">
              <Lock className="w-5 h-5 text-white" />
            </div>
            <h2 className="text-lg font-semibold text-gray-900">Change Password</h2>
          </div>
          <form onSubmit={handlePasswordSubmit} className="space-y-4 max-w-md">
            {passwordMsg && (
              <motion.div initial={{ opacity: 0, y: -5 }} animate={{ opacity: 1, y: 0 }}
                className="bg-green-50 border border-green-200 text-green-600 px-4 py-3 rounded-xl text-sm flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4" /> {passwordMsg}
              </motion.div>
            )}
            {passwordErr && (
              <motion.div initial={{ opacity: 0, y: -5 }} animate={{ opacity: 1, y: 0 }}
                className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-xl text-sm flex items-center gap-2">
                <AlertCircle className="w-4 h-4" /> {passwordErr}
              </motion.div>
            )}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Current Password</label>
              <input type="password" required value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} className="glass-input w-full" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">New Password</label>
              <input type="password" required minLength={6} value={newPassword} onChange={(e) => setNewPassword(e.target.value)} className="glass-input w-full" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Confirm New Password</label>
              <input type="password" required minLength={6} value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} className="glass-input w-full" />
            </div>
            <motion.button whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}
              type="submit" disabled={changePasswordMutation.isPending}
              className="bg-gray-900 text-white px-5 py-2.5 rounded-xl text-sm font-semibold hover:bg-gray-800 transition-all disabled:opacity-50">
              {changePasswordMutation.isPending ? 'Changing...' : 'Change Password'}
            </motion.button>
          </form>
        </motion.div>
      </div>
    </div>
  )
}
