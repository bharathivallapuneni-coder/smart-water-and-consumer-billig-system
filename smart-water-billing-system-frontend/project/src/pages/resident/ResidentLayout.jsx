import React from 'react'
import { Outlet } from 'react-router-dom'
import { LayoutDashboard, Gauge, Receipt, History, Wallet, UserCircle, Bell } from 'lucide-react'
import DashboardShell from '../../components/DashboardShell'

const navItems = [
  { to: '/resident', label: 'Overview', icon: LayoutDashboard, end: true },
  { to: '/resident/alerts', label: 'Alerts & Warnings', icon: Bell },
  { to: '/resident/meter', label: 'Meter Reading', icon: Gauge },
  { to: '/resident/bill', label: 'Current Bill', icon: Receipt },
  { to: '/resident/history', label: 'Previous Bills', icon: History },
  { to: '/resident/payments', label: 'Payment History', icon: Wallet },
  { to: '/resident/profile', label: 'Profile', icon: UserCircle }
]

export default function ResidentLayout() {
  return (
    <DashboardShell navItems={navItems} roleLabel="Resident">
      <Outlet />
    </DashboardShell>
  )
}
