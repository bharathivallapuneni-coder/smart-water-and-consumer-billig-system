import React, { useEffect, useState } from 'react'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts'
import { useAuth } from '../../context/AuthContext'
import { fetchBillsForBuilding, fetchMeterReadings } from '../../api/services'
import { PageHeader, Loader } from '../../components/UiBits'
import Panel from '../../components/Panel'

export default function Reports() {
  const { user } = useAuth()
  const [bills, setBills] = useState([])
  const [readings, setReadings] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([fetchBillsForBuilding(user.id), fetchMeterReadings(user.id)]).then(([b, r]) => {
      setBills(b)
      setReadings(r)
      setLoading(false)
    })
  }, [user.id])

  if (loading) return <Loader label="Loading reports" />

  const byMonth = {}
  bills.forEach((b) => {
    const key = `${b.month.slice(0, 3)} ${b.year}`
    byMonth[key] = byMonth[key] || { month: key, billed: 0, collected: 0 }
    byMonth[key].billed += b.amount
    if (b.status === 'PAID') byMonth[key].collected += b.amount
  })
  const chartData = Object.values(byMonth)

  const usageByMonth = {}
  readings.forEach((r) => {
    const key = `${r.month.slice(0, 3)} ${r.year}`
    usageByMonth[key] = (usageByMonth[key] || 0) + r.usage
  })
  const usageData = Object.entries(usageByMonth).map(([month, units]) => ({ month, units }))

  return (
    <div>
      <PageHeader eyebrow="Insights" title="Reports" subtitle="Billed vs. collected amounts and total water usage by month." />

      <div className="grid gap-5 lg:grid-cols-2">
        <Panel title="Billed vs collected" eyebrow="Amount in ₹">
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#E1EDED" />
                <XAxis dataKey="month" tick={{ fontSize: 11, fontFamily: 'IBM Plex Mono' }} stroke="#4A5D63" />
                <YAxis tick={{ fontSize: 11, fontFamily: 'IBM Plex Mono' }} stroke="#4A5D63" />
                <Tooltip contentStyle={{ borderRadius: 10, border: '1px solid #E1EDED', fontSize: 12 }} />
                <Bar dataKey="billed" fill="#7EC2E3" radius={[6, 6, 0, 0]} />
                <Bar dataKey="collected" fill="#3C8EB7" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Panel>

        <Panel title="Total consumption" eyebrow="Units per month" delay={0.05}>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={usageData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#E1EDED" />
                <XAxis dataKey="month" tick={{ fontSize: 11, fontFamily: 'IBM Plex Mono' }} stroke="#4A5D63" />
                <YAxis tick={{ fontSize: 11, fontFamily: 'IBM Plex Mono' }} stroke="#4A5D63" />
                <Tooltip contentStyle={{ borderRadius: 10, border: '1px solid #E1EDED', fontSize: 12 }} />
                <Bar dataKey="units" fill="#E8A33D" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Panel>
      </div>
    </div>
  )
}
