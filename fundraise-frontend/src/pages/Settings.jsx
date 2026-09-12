import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { profileAPI } from '../lib/api'
import { useAuth } from '../hooks/useAuth'

export default function Settings() {
  const [searchParams] = useSearchParams()
  const queryClient = useQueryClient()
  const { login, user } = useAuth()
  const upgraded = searchParams.get('upgraded') === 'true'

  // Profile form
  const [name, setName] = useState(user?.name || '')
  const [email, setEmail] = useState(user?.email || '')
  const [profileMsg, setProfileMsg] = useState('')
  const [profileErr, setProfileErr] = useState('')

  // Password form
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
      // Update local auth state
      login(localStorage.getItem('token'), {
        ...user,
        name: res.data.name,
        email: res.data.email,
      })
      queryClient.invalidateQueries({ queryKey: ['profile'] })
    },
    onError: (err) => {
      setProfileErr(err.response?.data?.message || 'Failed to update profile')
      setProfileMsg('')
    },
  })

  const changePasswordMutation = useMutation({
    mutationFn: (data) => profileAPI.changePassword(data),
    onSuccess: () => {
      setPasswordMsg('Password changed successfully')
      setPasswordErr('')
      setCurrentPassword('')
      setNewPassword('')
      setConfirmPassword('')
    },
    onError: (err) => {
      setPasswordErr(err.response?.data?.message || 'Failed to change password')
      setPasswordMsg('')
    },
  })

  const handleProfileSubmit = (e) => {
    e.preventDefault()
    setProfileMsg('')
    setProfileErr('')
    updateProfileMutation.mutate({ name, email })
  }

  const handlePasswordSubmit = (e) => {
    e.preventDefault()
    setPasswordMsg('')
    setPasswordErr('')

    if (newPassword !== confirmPassword) {
      setPasswordErr('New passwords do not match')
      return
    }

    changePasswordMutation.mutate({ currentPassword, newPassword })
  }

  const planColors = {
    FREE: 'bg-gray-100 text-gray-700',
    PRO: 'bg-indigo-100 text-indigo-700',
    ADVISOR: 'bg-purple-100 text-purple-700',
  }

  const planLabels = {
    FREE: 'Free',
    PRO: 'Pro',
    ADVISOR: 'Advisor',
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
      </div>
    )
  }

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">Settings</h1>
        <p className="text-sm text-gray-500">Manage your account settings and preferences</p>
      </div>

      {upgraded && (
        <div className="mb-6 bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg text-sm">
          🎉 Your plan has been upgraded! Changes are now active.
        </div>
      )}

      <div className="space-y-8">
        {/* Plan Info */}
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Current Plan</h2>
          <div className="flex items-center gap-4">
            <span className={`text-sm px-3 py-1 rounded-full font-medium ${planColors[profile?.plan] || planColors.FREE}`}>
              {planLabels[profile?.plan] || 'Free'}
            </span>
            <span className="text-sm text-gray-500">
              {profile?.plan === 'FREE' ? '1 company, basic checks' : 'Unlimited companies, full features'}
            </span>
            {profile?.plan === 'FREE' && (
              <a href="/pricing" className="text-sm text-indigo-600 hover:text-indigo-500 font-medium">
                Upgrade →
              </a>
            )}
          </div>
        </div>

        {/* Profile Form */}
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Profile</h2>
          <form onSubmit={handleProfileSubmit} className="space-y-4 max-w-md">
            {profileMsg && (
              <div className="bg-green-50 border border-green-200 text-green-600 px-4 py-3 rounded-lg text-sm">
                {profileMsg}
              </div>
            )}
            {profileErr && (
              <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg text-sm">
                {profileErr}
              </div>
            )}

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Full Name</label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500"
              />
            </div>

            <div className="flex items-center gap-2 text-sm">
              {profile?.verified ? (
                <span className="text-green-600 flex items-center gap-1">
                  <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                  </svg>
                  Email verified
                </span>
              ) : (
                <span className="text-yellow-600">Email not verified</span>
              )}
            </div>

            <button
              type="submit"
              disabled={updateProfileMutation.isPending}
              className="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50"
            >
              {updateProfileMutation.isPending ? 'Saving...' : 'Save Changes'}
            </button>
          </form>
        </div>

        {/* Password Form */}
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Change Password</h2>
          <form onSubmit={handlePasswordSubmit} className="space-y-4 max-w-md">
            {passwordMsg && (
              <div className="bg-green-50 border border-green-200 text-green-600 px-4 py-3 rounded-lg text-sm">
                {passwordMsg}
              </div>
            )}
            {passwordErr && (
              <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg text-sm">
                {passwordErr}
              </div>
            )}

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Current Password</label>
              <input
                type="password"
                required
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">New Password</label>
              <input
                type="password"
                required
                minLength={6}
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Confirm New Password</label>
              <input
                type="password"
                required
                minLength={6}
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500"
              />
            </div>

            <button
              type="submit"
              disabled={changePasswordMutation.isPending}
              className="px-4 py-2 bg-gray-900 text-white rounded-lg text-sm font-medium hover:bg-gray-800 disabled:opacity-50"
            >
              {changePasswordMutation.isPending ? 'Changing...' : 'Change Password'}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}
