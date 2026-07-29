import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { Building2, ClipboardCheck, ClipboardList, Users } from 'lucide-react'
import { fetchAdminStats, fetchBuildings } from '../../api/services'
import { PageHeader, StatCard, Loader } from '../../components/UiBits'
import Panel from '../../components/Panel'
import StatusPill from '../../components/StatusPill'
import RippleButton from '../../components/RippleButton'

export default function Overview() {
  const [stats, setStats] = useState(null)
  const [buildings, setBuildings] = useState([])
  const [recent, setRecent] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([fetchAdminStats(), fetchBuildings()]).then(([s, b]) => {
      setStats(s)
      setBuildings(b)
      setRecent(b.slice(0, 5))
      setLoading(false)
    })
  }, [])

  if (loading) return <Loader label="Loading overview" />

  const approvalRate = stats.totalBuildings ? Math.round((stats.approved / stats.totalBuildings) * 100) : 0
  const residentsPerBuilding = stats.totalBuildings ? (stats.totalResidents / stats.totalBuildings).toFixed(1) : '0.0'
  const chartData = Array.from({ length: 6 }, (_, index) => {
    const date = new Date()
    date.setMonth(date.getMonth() - (5 - index))

    const approvedCount = buildings.filter((building) => {
      if (building.status !== 'APPROVED') return false
      const createdAt = new Date(building.createdAt)
      return createdAt.getMonth() === date.getMonth() && createdAt.getFullYear() === date.getFullYear()
    }).length

    return {
      month: date.toLocaleString('en-US', { month: 'short' }),
      approved: approvedCount
    }
  })

  return (
    <div>
      <PageHeader eyebrow="Super Admin" title="Network overview" subtitle="Every verified building and pending request in one place." />

      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard icon={ClipboardList} label="Pending requests" value={stats.pending} tone="amber" delay={0.02} />
        <StatCard icon={ClipboardCheck} label="Approved buildings" value={stats.approved} tone="flow" delay={0.06} />
        <StatCard icon={Building2} label="Total buildings" value={stats.totalBuildings} tone="ink" delay={0.1} />
        <StatCard icon={Users} label="Total residents" value={stats.totalResidents} tone="flow" delay={0.14} />
      </div>

      <div className="mt-6 grid gap-4 xl:grid-cols-[1.1fr_0.9fr]">
        <Panel title="Analytics" eyebrow="Network health">
          <div className="grid gap-3 sm:grid-cols-3">
            <div className="rounded-2xl bg-flow-50 p-4">
              <p className="text-xs uppercase tracking-[0.2em] text-slate">Approval rate</p>
              <p className="mt-2 font-display text-2xl font-semibold text-ink">{approvalRate}%</p>
            </div>
            <div className="rounded-2xl bg-ink-50 p-4">
              <p className="text-xs uppercase tracking-[0.2em] text-slate">Residents / building</p>
              <p className="mt-2 font-display text-2xl font-semibold text-ink">{residentsPerBuilding}</p>
            </div>
            <div className="rounded-2xl bg-amber-50 p-4">
              <p className="text-xs uppercase tracking-[0.2em] text-slate">Pending review</p>
              <p className="mt-2 font-display text-2xl font-semibold text-ink">{stats.pending}</p>
            </div>
          </div>
          <div className="mt-4 h-56">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                <XAxis dataKey="month" tickLine={false} axisLine={false} />
                <YAxis allowDecimals={false} tickLine={false} axisLine={false} />
                <Tooltip />
                <Line type="monotone" dataKey="approved" stroke="#3b82f6" strokeWidth={3} dot={{ r: 4 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </Panel>

        <Panel
          title="Recent submissions"
          eyebrow="Latest activity"
          action={
            <Link to="/super-admin/requests">
              <RippleButton variant="subtle">Review requests</RippleButton>
            </Link>
          }
        >
          <div className="divide-y divide-ink-100/70">
            {recent.map((b) => (
              <div key={b.id} className="flex items-center justify-between py-3 first:pt-0 last:pb-0">
                <div>
                  <p className="font-display text-sm font-semibold text-ink">{b.buildingName}</p>
                  <p className="text-xs text-slate">{b.ownerName} · {b.location}</p>
                </div>
                <StatusPill status={b.status} />
              </div>
            ))}
          </div>
        </Panel>
      </div>
    </div>
  )
}
