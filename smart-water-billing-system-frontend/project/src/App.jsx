import React from 'react'
import { Routes, Route } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'

import Landing from './pages/Landing'
import Login from './pages/auth/Login'
import OwnerRegister from './pages/auth/OwnerRegister'

import SuperAdminLayout from './pages/superadmin/SuperAdminLayout'
import Overview from './pages/superadmin/Overview'
import PendingRequests from './pages/superadmin/PendingRequests'
import ApprovedBuildings from './pages/superadmin/ApprovedBuildings'

import OwnerLayout from './pages/owner/OwnerLayout'
import OwnerDashboard from './pages/owner/OwnerDashboard'
import OwnerProfile from './pages/owner/Profile'
import Residents from './pages/owner/Residents'
import MeterEntry from './pages/owner/MeterEntry'
import BillGeneration from './pages/owner/BillGeneration'
import PaymentStatus from './pages/owner/PaymentStatus'
import Reports from './pages/owner/Reports'

import ResidentLayout from './pages/resident/ResidentLayout'
import ResidentDashboard from './pages/resident/ResidentDashboard'
import ResidentProfile from './pages/resident/Profile'
import MeterReading from './pages/resident/MeterReading'
import CurrentBill from './pages/resident/CurrentBill'
import PreviousBills from './pages/resident/PreviousBills'
import PaymentHistory from './pages/resident/PaymentHistory'

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Landing />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<OwnerRegister />} />

      <Route
        path="/super-admin"
        element={
          <ProtectedRoute allow={['SUPER_ADMIN']}>
            <SuperAdminLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Overview />} />
        <Route path="requests" element={<PendingRequests />} />
        <Route path="buildings" element={<ApprovedBuildings />} />
      </Route>

      <Route
        path="/owner"
        element={
          <ProtectedRoute allow={['BUILDING_OWNER']}>
            <OwnerLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<OwnerDashboard />} />
        <Route path="profile" element={<OwnerProfile />} />
        <Route path="residents" element={<Residents />} />
        <Route path="meter-entry" element={<MeterEntry />} />
        <Route path="bills" element={<BillGeneration />} />
        <Route path="payments" element={<PaymentStatus />} />
        <Route path="reports" element={<Reports />} />
      </Route>

      <Route
        path="/resident"
        element={
          <ProtectedRoute allow={['RESIDENT']}>
            <ResidentLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<ResidentDashboard />} />
        <Route path="profile" element={<ResidentProfile />} />
        <Route path="meter" element={<MeterReading />} />
        <Route path="bill" element={<CurrentBill />} />
        <Route path="history" element={<PreviousBills />} />
        <Route path="payments" element={<PaymentHistory />} />
      </Route>

      <Route path="*" element={<Landing />} />
    </Routes>
  )
}
