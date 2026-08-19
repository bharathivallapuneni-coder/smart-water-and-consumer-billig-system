import React from 'react'
import { Outlet } from 'react-router-dom'
import { LayoutDashboard, Users, Gauge, Receipt, Wallet, BarChart3, UserCircle, Sliders, Truck, Calendar } from 'lucide-react'
import DashboardShell from '../../components/DashboardShell'

const navItems = [
  { to: '/owner/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/owner/tariffs', label: 'Tariff Config', icon: Sliders },
  { to: '/owner/water-purchases', label: 'Water Purchases', icon: Truck },
  { to: '/owner/billing-cycles', label: 'Billing Cycles', icon: Calendar },
  { to: '/owner/residents', label: 'Residents', icon: Users },
  { to: '/owner/meter-entry', label: 'Meter Entry', icon: Gauge },
  { to: '/owner/bill-generation', label: 'Bill Generation', icon: Receipt },
  { to: '/owner/payment-status', label: 'Payment Status', icon: Wallet },
  { to: '/owner/reports', label: 'Reports', icon: BarChart3 },
  { to: '/owner/profile', label: 'Profile', icon: UserCircle }
]

export default function OwnerLayout() {
  return (
    <DashboardShell navItems={navItems} roleLabel="Building Owner">
      <Outlet />
    </DashboardShell>
  )
}
