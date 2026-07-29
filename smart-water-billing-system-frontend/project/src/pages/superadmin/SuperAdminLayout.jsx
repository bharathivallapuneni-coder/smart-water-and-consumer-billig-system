import React from 'react'
import { Outlet } from 'react-router-dom'
import { LayoutDashboard, ClipboardList, Building2 } from 'lucide-react'
import DashboardShell from '../../components/DashboardShell'

const navItems = [
  { to: '/super-admin', label: 'Overview', icon: LayoutDashboard, end: true },
  { to: '/super-admin/requests', label: 'Pending Requests', icon: ClipboardList },
  { to: '/super-admin/buildings', label: 'Approved Buildings', icon: Building2 }
]

export default function SuperAdminLayout() {
  return (
    <DashboardShell navItems={navItems} roleLabel="Super Admin">
      <Outlet />
    </DashboardShell>
  )
}
