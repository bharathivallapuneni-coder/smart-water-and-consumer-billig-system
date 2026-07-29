import React from 'react'
import { Building2, Home, User } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { PageHeader } from '../../components/UiBits'
import Panel from '../../components/Panel'

export default function Profile() {
  const { user } = useAuth()
  const rows = [
    { icon: User, label: 'Resident name', value: user.name },
    { icon: Home, label: 'Flat number', value: user.flatNumber },
    { icon: Building2, label: 'Building', value: user.buildingName }
  ]

  return (
    <div>
      <PageHeader eyebrow="Account" title="Profile" subtitle="Your resident details as set up by your building owner." />
      <Panel className="max-w-lg">
        <div className="space-y-4">
          {rows.map((r) => (
            <div key={r.label} className="flex items-center gap-3 border-b border-ink-100/70 pb-4 last:border-0 last:pb-0">
              <div className="grid h-9 w-9 place-items-center rounded-lg bg-flow-100 text-flow-700">
                <r.icon size={16} />
              </div>
              <div>
                <p className="panel-label">{r.label}</p>
                <p className="text-sm font-medium text-ink">{r.value}</p>
              </div>
            </div>
          ))}
        </div>
      </Panel>
    </div>
  )
}
