import React, { useEffect, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import toast from 'react-hot-toast'
import { UserPlus, Users, X, Pencil, Trash2 } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { fetchResidents, createResident, updateResident, deleteResident } from '../../api/services'
import { PageHeader, Loader, EmptyState, Field, inputClass } from '../../components/UiBits'
import Panel from '../../components/Panel'
import RippleButton from '../../components/RippleButton'

const empty = { flatNumber: '', name: '', phone: '', username: '', password: '' }

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
      flatNumber: resident.flatNumber,
      name: resident.name,
      phone: resident.phone,
      username: resident.username,
      password: resident.password
    })
    setShowForm(true)
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setBusy(true)
    try {
      if (editingResidentId) {
        await updateResident(user.id, editingResidentId, form)
        toast.success(`${form.name} was updated`)
      } else {
        await createResident(user.id, form)
        toast.success(`Account created for ${form.flatNumber}`)
      }
      closeForm()
      load()
    } catch (err) {
      toast.error(err.message || (editingResidentId ? 'Could not update resident' : 'Could not add resident'))
    } finally {
      setBusy(false)
    }
  }

  async function handleDeleteResident(resident) {
    const confirmed = window.confirm(`Delete ${resident.name} from this building?`)
    if (!confirmed) return

    try {
      await deleteResident(user.id, resident.id)
      toast.success(`${resident.name} was removed`)
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
        subtitle="Assign flats, issue login credentials and send invitations."
        action={
          <RippleButton onClick={openAddForm}>
            <UserPlus size={16} /> Add resident
          </RippleButton>
        }
      />

      {loading ? (
        <Loader label="Loading residents" />
      ) : residents.length === 0 ? (
        <EmptyState icon={Users} title="No residents yet" subtitle="Add your first resident to start generating bills." />
      ) : (
        <Panel className="!p-0">
          <div className="divide-y divide-ink-100/70">
            {residents.map((r) => (
              <div key={r.id} className="flex flex-col gap-3 p-5 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <p className="font-display text-sm font-semibold text-ink">{r.flatNumber} · {r.name}</p>
                  <p className="mt-0.5 font-mono text-xs text-slate">{r.username} · {r.phone}</p>
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
            ))}
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
                <h3 className="font-display text-lg font-semibold text-ink">{editingResidentId ? 'Edit resident' : 'Add resident'}</h3>
                <button onClick={closeForm} className="text-slate hover:text-ink">
                  <X size={18} />
                </button>
              </div>
              <form onSubmit={handleSubmit} className="space-y-3.5">
                <Field label="Flat number">
                  <input required className={inputClass} value={form.flatNumber} onChange={(e) => update('flatNumber', e.target.value)} placeholder="A-103" />
                </Field>
                <Field label="Resident name">
                  <input required className={inputClass} value={form.name} onChange={(e) => update('name', e.target.value)} placeholder="Full name" />
                </Field>
                <Field label="Phone number">
                  <input required className={inputClass} value={form.phone} onChange={(e) => update('phone', e.target.value)} placeholder="10-digit number" />
                </Field>
                <Field label="Username">
                  <input required className={inputClass} value={form.username} onChange={(e) => update('username', e.target.value)} placeholder="e.g. name.a103" />
                </Field>
                <Field label="Temporary password">
                  <input required className={inputClass} value={form.password} onChange={(e) => update('password', e.target.value)} placeholder="Assign a password" />
                </Field>
                <RippleButton type="submit" disabled={busy} className="w-full">
                  {busy ? (editingResidentId ? 'Saving…' : 'Creating…') : (editingResidentId ? 'Save changes' : 'Create account & invite')}
                </RippleButton>
              </form>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  )
}
