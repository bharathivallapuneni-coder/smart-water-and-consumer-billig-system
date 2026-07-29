import React, { createContext, useContext, useEffect, useState } from 'react'
import { loginRequest } from '../api/services'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const stored = localStorage.getItem('awb_user')
    if (stored) setUser(JSON.parse(stored))
    setLoading(false)
  }, [])

  async function login({ role, username, password }) {
    const { token, user: u } = await loginRequest({ role, username, password })
    localStorage.setItem('awb_token', token)
    localStorage.setItem('awb_user', JSON.stringify(u))
    setUser(u)
    return u
  }

  function logout() {
    localStorage.removeItem('awb_token')
    localStorage.removeItem('awb_user')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
