import React from 'react'
import { Building2, MapPin, Phone, Mail, User } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { PageHeader } from '../../components/UiBits'
import Panel from '../../components/Panel'

export default function Profile() {
  const { user } = useAuth()

  const rows = [
    { icon: Building2, label: 'Building name', value: user.buildingName },
    { icon: User, label: 'Owner name', value: user.name },
    { icon: MapPin, label: 'Building ID', value: user.id }
  ]

  return (
    <div>
      <PageHeader eyebrow="Account" title="Profile" subtitle="Your registered building details on file with the Super Admin." />
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
        <p className="mt-5 text-xs text-slate">
          To update building or contact details, submit a change request to the Super Admin (backend endpoint:
          <span className="font-mono"> PATCH /api/owners/{'{id}'}/profile</span>).
        </p>
      </Panel>
    </div>
  )
}
