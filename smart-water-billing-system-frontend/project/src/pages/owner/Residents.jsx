import React, { useEffect, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import toast from 'react-hot-toast'
import { UserPlus, Users, X, Pencil, Trash2, Mail, CheckCircle2, Clock } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { fetchResidents, createResident, updateResident, deleteResident } from '../../api/services'
import { PageHeader, Loader, EmptyState, Field, inputClass } from '../../components/UiBits'
import Panel from '../../components/Panel'
import RippleButton from '../../components/RippleButton'

const empty = { name: '', email: '', phone: '', flatNumber: '', blockNumber: '' }

export default function Residents() {
  const { user } = useAuth()
  const [residents, setResidents] = useState([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [editingResidentId, setEditingResidentId] = useState(null)
  const [form, setForm] = useState(empty)
  const [busy, setBusy] = useState(false)

  async function load() {
    setLoading(true)
    setResidents(await fetchResidents(user.id))
    setLoading(false)
  }

  useEffect(() => {
    load()
  }, [user.id])

  function update(key, val) {
    setForm((f) => ({ ...f, [key]: val }))
  }

  function closeForm() {
    setShowForm(false)
    setEditingResidentId(null)
    setForm(empty)
  }

  function openAddForm() {
    setEditingResidentId(null)
    setForm(empty)
    setShowForm(true)
  }

  function openEditForm(resident) {
    setEditingResidentId(resident.id)
    setForm({
      name: resident.name || resident.ownerName || '',
      email: resident.email || '',
      phone: resident.phone || resident.contactPhone || '',
      flatNumber: resident.flatNumber || resident.householdNumber || '',
      blockNumber: resident.blockNumber || ''
    })
    setShowForm(true)
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setBusy(true)
    try {
      if (editingResidentId) {
        await updateResident(user.id, editingResidentId, form)
        toast.success(`${form.name} details updated`)
      } else {
        const res = await createResident(user.id, form)
        const successMsg = typeof res === 'string' ? res : (res?.message || `Invitation sent to ${form.email}`)
        toast.success(successMsg)
      }
      closeForm()
      load()
    } catch (err) {
      const data = err.response?.data
      let errMsg = err.message || (editingResidentId ? 'Could not update resident' : 'Could not send invitation')
      if (data) {
        if (typeof data.data === 'object' && data.data !== null && !Array.isArray(data.data)) {
          const firstField = Object.keys(data.data)[0]
          if (firstField && data.data[firstField]) {
            errMsg = `${data.data[firstField]}`
          } else if (data.message) {
            errMsg = data.message
          }
        } else if (data.message) {
          errMsg = data.message
        }
      }
      toast.error(errMsg)
    } finally {
      setBusy(false)
    }
  }

  async function handleDeleteResident(resident) {
    const confirmed = window.confirm(`Delete ${resident.name || resident.ownerName} from this building?`)
    if (!confirmed) return

    try {
      await deleteResident(user.id, resident.id)
      toast.success(`${resident.name || resident.ownerName} was removed`)
      load()
    } catch (err) {
      toast.error(err.message || 'Could not delete resident')
    }
  }

  return (
    <div>
      <PageHeader
        eyebrow="Occupancy"
        title="Residents"
        subtitle="Manage flat assignments and invite residents to create their HydroBill accounts."
        action={
          <RippleButton onClick={openAddForm}>
            <UserPlus size={16} /> Add resident
          </RippleButton>
        }
      />

      {loading ? (
        <Loader label="Loading residents" />
      ) : residents.length === 0 ? (
        <EmptyState icon={Users} title="No residents yet" subtitle="Add your first resident to send an invitation link." />
      ) : (
        <Panel className="!p-0">
          <div className="divide-y divide-ink-100/70">
            {residents.map((r) => {
              const resName = r.name || r.ownerName || 'Resident'
              const flatStr = r.blockNumber ? `${r.blockNumber} - ${r.flatNumber || r.householdNumber}` : (r.flatNumber || r.householdNumber)
              const status = r.invitationStatus || (r.username ? 'ACCEPTED' : 'PENDING')
              return (
                <div key={r.id} className="flex flex-col gap-3 p-5 sm:flex-row sm:items-center sm:justify-between">
                  <div>
                    <div className="flex items-center gap-2">
                      <p className="font-display text-sm font-semibold text-ink">{flatStr} · {resName}</p>
                      {status === 'ACCEPTED' ? (
                        <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2 py-0.5 font-mono text-[10px] font-medium text-emerald-700">
                          <CheckCircle2 size={11} /> Account Active
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 rounded-full bg-amber-50 px-2 py-0.5 font-mono text-[10px] font-medium text-amber-700">
                          <Clock size={11} /> Invite Sent
                        </span>
                      )}
                    </div>
                    <p className="mt-0.5 font-mono text-xs text-slate">
                      {r.email ? `${r.email} · ` : ''}{r.phone || r.contactPhone || 'No phone'}
                      {r.username ? ` · @${r.username}` : ''}
                    </p>
                  </div>
                  <div className="flex flex-wrap items-center gap-2">
                    <RippleButton variant="subtle" onClick={() => openEditForm(r)}>
                      <Pencil size={13} /> Edit
                    </RippleButton>
                    <RippleButton variant="danger" onClick={() => handleDeleteResident(r)}>
                      <Trash2 size={13} /> Delete
                    </RippleButton>
                  </div>
                </div>
              )
            })}
          </div>
        </Panel>
      )}

      <AnimatePresence>
        {showForm && (
          <motion.div className="fixed inset-0 z-50 grid place-items-center bg-ink/50 p-4" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}>
            <motion.div
              initial={{ opacity: 0, y: 20, scale: 0.97 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: 10, scale: 0.97 }}
              className="w-full max-w-md rounded-2xl bg-white p-6 shadow-panel-lg"
            >
              <div className="mb-4 flex items-center justify-between">
                <div>
                  <h3 className="font-display text-lg font-semibold text-ink">{editingResidentId ? 'Edit resident details' : 'Invite Resident'}</h3>
                  {!editingResidentId && (
                    <p className="text-xs text-slate mt-0.5">An invitation email with a unique account creation link will be sent.</p>
                  )}
                </div>
                <button onClick={closeForm} className="text-slate hover:text-ink">
                  <X size={18} />
                </button>
              </div>
              <form onSubmit={handleSubmit} className="space-y-3.5">
                <Field label="Full Name">
                  <input required className={inputClass} value={form.name} onChange={(e) => update('name', e.target.value)} placeholder="e.g. Priya Sharma" />
                </Field>
                <Field label="Email ID">
                  <input required type="email" className={inputClass} value={form.email} onChange={(e) => update('email', e.target.value)} placeholder="priya@gmail.com" />
                </Field>
                <Field label="Phone Number">
                  <input required className={inputClass} value={form.phone} onChange={(e) => update('phone', e.target.value)} placeholder="10-digit number (e.g. 9876543210)" />
                </Field>
                <div className="grid grid-cols-2 gap-3">
                  <Field label="Flat Number">
                    <input required className={inputClass} value={form.flatNumber} onChange={(e) => update('flatNumber', e.target.value)} placeholder="A-103" />
                  </Field>
                  <Field label="Block Number">
                    <input className={inputClass} value={form.blockNumber} onChange={(e) => update('blockNumber', e.target.value)} placeholder="Block A" />
                  </Field>
                </div>
                <RippleButton type="submit" disabled={busy} className="w-full mt-2">
                  {busy ? (editingResidentId ? 'Saving…' : 'Sending Invitation…') : (editingResidentId ? 'Save changes' : 'Create & Invite')}
                </RippleButton>
              </form>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  )
}
