import React, { useEffect, useState } from 'react'
import { Receipt } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { fetchResidentBills } from '../../api/services'
import { PageHeader, Loader, EmptyState } from '../../components/UiBits'
import Panel from '../../components/Panel'
import StatusPill from '../../components/StatusPill'

export default function PreviousBills() {
  const { user } = useAuth()
  const [bills, setBills] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchResidentBills(user?.id).then((b) => {
      setBills(Array.isArray(b) ? b : [])
      setLoading(false)
    }).catch(() => {
      setBills([])
      setLoading(false)
    })
  }, [user?.id])

  if (loading) return <Loader label="Loading bill history" />

  const safeBills = Array.isArray(bills) ? bills : []

  return (
    <div>
      <PageHeader eyebrow="Records" title="Previous bills" subtitle="Every bill generated for your flat, oldest to newest." />

      {safeBills.length === 0 ? (
        <EmptyState icon={Receipt} title="No bills yet" subtitle="Bills will appear here once your owner generates one." />
      ) : (
        <Panel className="!p-0">
          <div className="divide-y divide-ink-100/70">
            {safeBills.map((b) => (
              <div key={b.id} className="flex items-center justify-between p-5">
                <div>
                  <p className="font-display text-sm font-semibold text-ink">{b.month} {b.year}</p>
                  <p className="mt-0.5 text-xs text-slate">{b.usage} units consumed</p>
                </div>
                <div className="flex items-center gap-4">
                  <span className="font-mono text-sm text-ink">₹{b.amount}</span>
                  <StatusPill status={b.status} />
                </div>
              </div>
            ))}
          </div>
        </Panel>
      )}
    </div>
  )
}
