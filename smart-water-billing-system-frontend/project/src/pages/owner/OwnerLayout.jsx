import React from 'react'
import { Outlet } from 'react-router-dom'
import { LayoutDashboard, Users, Gauge, Receipt, Wallet, BarChart3, UserCircle } from 'lucide-react'
import DashboardShell from '../../components/DashboardShell'

const navItems = [
  { to: '/owner', label: 'Overview', icon: LayoutDashboard, end: true },
  { to: '/owner/profile', label: 'Profile', icon: UserCircle },
  { to: '/owner/residents', label: 'Residents', icon: Users },
  { to: '/owner/meter-entry', label: 'Meter Entry', icon: Gauge },
  { to: '/owner/bills', label: 'Bill Generation', icon: Receipt },
  { to: '/owner/payments', label: 'Payment Status', icon: Wallet },
  { to: '/owner/reports', label: 'Reports', icon: BarChart3 }
]

export default function OwnerLayout() {
  return (
    <DashboardShell navItems={navItems} roleLabel="Building Owner">
      <Outlet />
    </DashboardShell>
  )
}
