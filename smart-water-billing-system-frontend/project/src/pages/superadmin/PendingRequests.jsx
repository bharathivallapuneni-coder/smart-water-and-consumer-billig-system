import React, { useEffect, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import toast from 'react-hot-toast'
import { Check, X, Phone, MapPin, User, Building2 } from 'lucide-react'
import { fetchBuildings, decideBuildingRequest } from '../../api/services'
import { PageHeader, Loader, EmptyState } from '../../components/UiBits'
import RippleButton from '../../components/RippleButton'

export default function PendingRequests() {
  const [buildings, setBuildings] = useState([])
  const [loading, setLoading] = useState(true)
  const [busyId, setBusyId] = useState(null)

  async function load() {
    setLoading(true)
    const data = await fetchBuildings()
    setBuildings(data.filter((b) => b.status === 'PENDING'))
    setLoading(false)
  }

  useEffect(() => {
    load()
  }, [])

  async function decide(id, decision) {
    setBusyId(id)
    try {
      await decideBuildingRequest(id, decision)
      toast.success(decision === 'APPROVED' ? 'Building approved' : 'Request rejected')
      setBuildings((prev) => prev.filter((b) => b.id !== id))
    } catch (err) {
      toast.error(err.message || 'Action failed')
    } finally {
      setBusyId(null)
    }
  }

  if (loading) return <Loader label="Loading requests" />

  return (
    <div>
      <PageHeader eyebrow="Verification queue" title="Pending building requests" subtitle="Confirm the details before granting the owner access." />

      {buildings.length === 0 ? (
        <EmptyState icon={Building2} title="No pending requests" subtitle="New building registrations will show up here for review." />
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          <AnimatePresence>
            {buildings.map((b) => (
              <motion.div
                key={b.id}
                layout
                initial={{ opacity: 0, y: 12 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, scale: 0.95 }}
                transition={{ duration: 0.3 }}
                className="rounded-2xl border border-ink-100 bg-white p-5 shadow-panel"
              >
                <div className="mb-4 flex items-start justify-between">
                  <div>
                    <p className="panel-label mb-1">Building</p>
                    <p className="font-display text-base font-semibold text-ink">{b.buildingName}</p>
                  </div>
                  <span className="rounded-full bg-amber-100 px-2.5 py-1 font-mono text-[10px] uppercase text-amber-600">Awaiting review</span>
                </div>

                <div className="space-y-2.5 text-sm text-slate">
                  <Row icon={User} label="Owner" value={b.ownerName} />
                  <Row icon={MapPin} label="Address" value={`${b.address}, ${b.location}`} />
                  <Row icon={Phone} label="Phone" value={b.phone} />
                </div>

                <div className="mt-5 flex gap-2.5">
                  <RippleButton
                    variant="danger"
                    className="flex-1 !px-4 !py-2 text-sm"
                    disabled={busyId === b.id}
                    onClick={() => decide(b.id, 'REJECTED')}
                  >
                    <X size={15} /> Reject
                  </RippleButton>
                  <RippleButton
                    variant="primary"
                    className="flex-1 !px-4 !py-2 text-sm"
                    disabled={busyId === b.id}
                    onClick={() => decide(b.id, 'APPROVED')}
                  >
                    <Check size={15} /> Approve
                  </RippleButton>
                </div>
              </motion.div>
            ))}
          </AnimatePresence>
        </div>
      )}
    </div>
  )
}

function Row({ icon: Icon, label, value }) {
  return (
    <div className="flex items-start gap-2.5">
      <Icon size={14} className="mt-0.5 shrink-0 text-flow-600" />
      <p>
        <span className="text-ink-400">{label}:</span> {value}
      </p>
    </div>
  )
}
