import React, { useEffect, useState } from 'react'
import { Building2 } from 'lucide-react'
import { fetchBuildings } from '../../api/services'
import { PageHeader, Loader, EmptyState } from '../../components/UiBits'
import Panel from '../../components/Panel'

export default function ApprovedBuildings() {
  const [buildings, setBuildings] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchBuildings().then((data) => {
      setBuildings(data.filter((b) => b.status === 'APPROVED'))
      setLoading(false)
    })
  }, [])

  if (loading) return <Loader label="Loading buildings" />

  return (
    <div>
      <PageHeader eyebrow="Live network" title="Approved buildings" subtitle={`${buildings.length} building${buildings.length !== 1 ? 's' : ''} currently active.`} />

      {buildings.length === 0 ? (
        <EmptyState icon={Building2} title="No approved buildings yet" subtitle="Approve a request to see it listed here." />
      ) : (
        <Panel className="!p-0">
          <div className="divide-y divide-ink-100/70">
            {buildings.map((b, i) => (
              <div key={b.id} className="flex flex-col justify-between gap-2 p-5 sm:flex-row sm:items-center">
                <div>
                  <p className="font-display text-sm font-semibold text-ink">{b.buildingName}</p>
                  <p className="mt-0.5 text-xs text-slate">{b.ownerName} · {b.address}, {b.location}</p>
                </div>
                <span className="font-mono text-xs text-slate">{b.phone}</span>
              </div>
            ))}
          </div>
        </Panel>
      )}
    </div>
  )
}
