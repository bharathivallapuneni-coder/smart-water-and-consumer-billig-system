import React, { useEffect, useState } from 'react'
import { useSearchParams, useNavigate, Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import toast from 'react-hot-toast'
import { Droplets, CheckCircle2, AlertTriangle, ShieldCheck, UserCheck } from 'lucide-react'
import { validateInvitationToken, activateResidentAccount } from '../../api/services'
import { Field, inputClass, Loader } from '../../components/UiBits'
import RippleButton from '../../components/RippleButton'
import PageTransition from '../../components/PageTransition'

export default function ActivateAccount() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = searchParams.get('token')

  const [loading, setLoading] = useState(true)
  const [validatingError, setValidatingError] = useState(null)
  const [inviteData, setInviteData] = useState(null)

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    async function checkToken() {
      if (!token) {
        setValidatingError('No invitation token provided. Please check your email invitation link.')
        setLoading(false)
        return
      }

      try {
        setLoading(true)
        const data = await validateInvitationToken(token)
        setInviteData(data)
      } catch (err) {
        setValidatingError(err.message || 'Invalid or expired invitation link. Please request a new invitation from your Building Owner.')
      } finally {
        setLoading(false)
      }
    }

    checkToken()
  }, [token])

  async function handleSubmit(e) {
    e.preventDefault()

    if (!username.trim() || username.trim().length < 3) {
      toast.error('Username must be at least 3 characters long')
      return
    }

    if (!password || password.length < 6) {
      toast.error('Password must be at least 6 characters long')
      return
    }

    if (password !== confirmPassword) {
      toast.error('Password and Confirm Password do not match')
      return
    }

    setSubmitting(true)

    try {
      await activateResidentAccount({
        token,
        username: username.trim(),
        password,
        confirmPassword
      })

      toast.success('Account created successfully. Please log in using your new credentials.')

      // Redirect to EXISTING COMMON LOGIN PAGE
      navigate('/login')
    } catch (err) {
      toast.error(err.message || 'Could not create account. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <PageTransition>
      <div className="min-h-screen bg-slate-50 grid place-items-center p-4">
        <div className="w-full max-w-md">
          {/* HydroBill Brand Header */}
          <div className="mb-6 text-center">
            <Link to="/" className="inline-flex items-center gap-2 font-display text-2xl font-bold tracking-tight text-ink">
              <span className="grid h-10 w-10 place-items-center rounded-xl bg-flow-600 text-white shadow-md shadow-flow-600/30">
                <Droplets size={22} className="fill-current" />
              </span>
              <span>Hydro<span className="text-flow-600">Bill</span></span>
            </Link>
          </div>

          <motion.div
            initial={{ opacity: 0, y: 15 }}
            animate={{ opacity: 1, y: 0 }}
            className="rounded-2xl bg-white p-7 shadow-panel border border-ink-100/70"
          >
            {loading ? (
              <Loader label="Validating your invitation link…" />
            ) : validatingError ? (
              <div className="text-center py-4 space-y-4">
                <div className="mx-auto w-12 h-12 rounded-full bg-rose-50 text-rose-600 grid place-items-center">
                  <AlertTriangle size={26} />
                </div>
                <div>
                  <h3 className="font-display text-lg font-semibold text-ink">Invalid Invitation Link</h3>
                  <p className="mt-1 text-sm text-slate">{validatingError}</p>
                </div>
                <RippleButton onClick={() => navigate('/login')} variant="subtle" className="w-full mt-2">
                  Return to Common Login
                </RippleButton>
              </div>
            ) : (
              <div>
                <div className="mb-6 text-center">
                  <h2 className="font-display text-2xl font-semibold text-ink">Create Your Account</h2>
                  <p className="mt-1 text-sm text-slate">Set up your username and password to complete registration.</p>
                </div>

                {/* Resident Details Banner */}
                {inviteData && (
                  <div className="mb-6 rounded-xl bg-flow-50/60 border border-flow-100 p-4 text-xs space-y-1">
                    <p className="font-medium text-flow-900 flex items-center gap-1.5 text-sm">
                      <UserCheck size={16} className="text-flow-600" />
                      <span>{inviteData.fullName}</span>
                    </p>
                    <p className="text-slate">
                      <strong className="text-ink font-semibold">Building:</strong> {inviteData.buildingName}
                    </p>
                    <p className="text-slate">
                      <strong className="text-ink font-semibold">Flat / Unit:</strong> {inviteData.blockNumber ? `${inviteData.blockNumber} - ${inviteData.flatNumber}` : inviteData.flatNumber}
                    </p>
                    <p className="text-slate">
                      <strong className="text-ink font-semibold">Email:</strong> {inviteData.email}
                    </p>
                  </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-4">
                  <Field label="Username">
                    <input
                      required
                      type="text"
                      className={inputClass}
                      value={username}
                      onChange={(e) => setUsername(e.target.value)}
                      placeholder="Choose a username (e.g. priya.a103)"
                      autoComplete="username"
                    />
                  </Field>

                  <Field label="Password">
                    <input
                      required
                      type="password"
                      className={inputClass}
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="Minimum 6 characters"
                      autoComplete="new-password"
                    />
                  </Field>

                  <Field label="Confirm Password">
                    <input
                      required
                      type="password"
                      className={inputClass}
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      placeholder="Re-enter password"
                      autoComplete="new-password"
                    />
                  </Field>

                  <RippleButton type="submit" disabled={submitting} className="w-full mt-2">
                    {submitting ? 'Creating Account…' : 'Create Account'}
                  </RippleButton>
                </form>

                <div className="mt-6 border-t border-ink-100/70 pt-4 text-center text-xs text-slate">
                  Already have an account?{' '}
                  <Link to="/login" className="font-medium text-flow-600 hover:text-flow-700 underline">
                    Log in here
                  </Link>
                </div>
              </div>
            )}
          </motion.div>
        </div>
      </div>
    </PageTransition>
  )
}
