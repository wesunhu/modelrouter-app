/**
 * React context: login state, session, and auth API calls.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

import { createContext, useContext, useState, useEffect, useCallback, ReactNode } from 'react'
import { api } from '../api/client'

interface AuthContextType {
  username: string | null
  loading: boolean
  needsInit: boolean | null
  login: (username: string, password: string) => Promise<void>
  init: (username: string, password: string) => Promise<void>
  logout: () => Promise<void>
  refresh: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [username, setUsername] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [needsInit, setNeedsInit] = useState<boolean | null>(null)

  const refresh = useCallback(async () => {
    try {
      const { data } = await api.get<{ username: string }>('/api/auth/me')
      setUsername(data.username)
      setNeedsInit(false)
    } catch (err: unknown) {
      const status = (err as { response?: { status?: number } })?.response?.status
      if (status === 401) {
        setUsername(null)
        try {
          const { data: initData } = await api.get<{ needsInit: boolean }>('/api/auth/needs-init')
          setNeedsInit(initData.needsInit)
        } catch (e: unknown) {
          const data = (e as { response?: { data?: { needsInit?: boolean } } })?.response?.data
          setNeedsInit(data?.needsInit === true ? true : false)
        }
      }
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    refresh()
  }, [refresh])

  const login = useCallback(async (u: string, p: string) => {
    const { data } = await api.post<{ username: string }>('/api/auth/login', { username: u, password: p })
    setUsername(data.username)
    setNeedsInit(false)
  }, [])

  const init = useCallback(async (u: string, p: string) => {
    await api.post('/api/auth/init', { username: u, password: p })
    setUsername(u)
    setNeedsInit(false)
  }, [])

  const logout = useCallback(async () => {
    await api.post('/api/auth/logout', {})
    setUsername(null)
  }, [])

  return (
    <AuthContext.Provider value={{ username, loading, needsInit, login, init, logout, refresh }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
