import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import toast from 'react-hot-toast'
import { Droplets, CheckCircle2 } from 'lucide-react'
import { registerBuildingOwner } from '../../api/services'
import RippleButton from '../../components/RippleButton'
import { Field, inputClass } from '../../components/UiBits'

const empty = {
  buildingName: '',
  ownerName: '',
  address: '',
  location: '',
  phone: '',
  email: '',
  username: '',
  password: '',
  confirmPassword: ''
}

export default function OwnerRegister() {
  const [form, setForm] = useState(empty)
  const [busy, setBusy] = useState(false)
  const [done, setDone] = useState(false)
  const navigate = useNavigate()

  function update(key, val) {
    setForm((f) => ({ ...f, [key]: val }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    if (form.password !== form.confirmPassword) {
      toast.error('Password and Confirm Password do not match!')
      return
    }
    setBusy(true)
    try {
      await registerBuildingOwner(form)
      setDone(true)
      toast.success('Registration request submitted')
    } catch (err) {
      toast.error(err.message || 'Could not submit request')
    } finally {
      setBusy(false)
    }
  }

  if (done) {
    return (
      <div className="grid min-h-screen place-items-center bg-foam px-6">
        <motion.div
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          className="w-full max-w-md rounded-2xl border border-ink-100 bg-white p-8 text-center shadow-panel-lg"
        >
          <div className="mx-auto grid h-14 w-14 place-items-center rounded-full bg-flow-100 text-flow-700">
            <CheckCircle2 size={26} />
          </div>
          <h1 className="mt-5 font-display text-xl font-semibold text-ink">Request submitted</h1>
          <p className="mt-2 text-sm text-slate">
            <strong className="text-ink">{form.buildingName}</strong> is now waiting for Super Admin verification. You'll be able
            to log in with the username <span className="font-mono text-flow-700">{form.username}</span> once it's approved.
          </p>
          <RippleButton className="mt-6 w-full" onClick={() => navigate('/login')}>
            Back to login
          </RippleButton>
        </motion.div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-foam px-6 py-12">
      <div className="mx-auto max-w-xl">
        <Link to="/" className="mb-8 flex items-center gap-2">
          <div className="grid h-9 w-9 place-items-center rounded-xl bg-flow-gradient text-white">
            <Droplets size={18} />
          </div>
          <span className="font-display text-lg font-semibold text-ink">HydroBill</span>
        </Link>

        <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} className="rounded-2xl border border-ink-100 bg-white p-7 shadow-panel">
          <p className="panel-label mb-1">Building owner</p>
          <h1 className="font-display text-2xl font-semibold text-ink">Register your building</h1>
          <p className="mt-1 text-sm text-slate">A Super Admin will verify these details before your account is activated.</p>

          <form onSubmit={handleSubmit} className="mt-6 grid gap-4 sm:grid-cols-2">
            <Field label="Building name">
              <input required className={inputClass} value={form.buildingName} onChange={(e) => update('buildingName', e.target.value)} placeholder="Green Valley Apartments" />
            </Field>
            <Field label="Owner name">
              <input required className={inputClass} value={form.ownerName} onChange={(e) => update('ownerName', e.target.value)} placeholder="Your full name" />
            </Field>
            <Field label="Address">
              <input required className={inputClass} value={form.address} onChange={(e) => update('address', e.target.value)} placeholder="Street, area" />
            </Field>
            <Field label="Location">
              <input required className={inputClass} value={form.location} onChange={(e) => update('location', e.target.value)} placeholder="City, state" />
            </Field>
            <Field label="Phone number">
              <input required className={inputClass} value={form.phone} onChange={(e) => update('phone', e.target.value)} placeholder="10-digit number" />
            </Field>
            <Field label="Email">
              <input required type="email" className={inputClass} value={form.email} onChange={(e) => update('email', e.target.value)} placeholder="you@example.com" />
            </Field>
            <Field label="Choose a username">
              <input required className={inputClass} value={form.username} onChange={(e) => update('username', e.target.value)} placeholder="e.g. yourname.owner" />
            </Field>
            <Field label="Choose a password">
              <input required type="password" className={inputClass} value={form.password} onChange={(e) => update('password', e.target.value)} placeholder="••••••••" />
            </Field>
            <Field label="Confirm password" className="sm:col-span-2">
              <input required type="password" className={inputClass} value={form.confirmPassword} onChange={(e) => update('confirmPassword', e.target.value)} placeholder="Re-enter password" />
            </Field>

            <RippleButton type="submit" disabled={busy} className="sm:col-span-2">
              {busy ? 'Submitting…' : 'Submit for approval'}
            </RippleButton>
          </form>
        </motion.div>
      </div>
    </div>
  )
}
