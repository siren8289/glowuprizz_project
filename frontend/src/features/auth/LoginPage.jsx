import { useState } from 'react'
import { api } from '../../shared/api'

export function LoginPage({ onLogin }) {
  const [error, setError] = useState('')
  const login = async (event) => {
    event.preventDefault()
    try {
      const data = await api(null, '/api/auth/login', { method: 'POST', body: JSON.stringify(Object.fromEntries(new FormData(event.currentTarget))) })
      localStorage.setItem('lm_token', data.token)
      onLogin(data.token)
    } catch (exception) { setError(exception.message) }
  }
  return <main className="login"><div className="brand"><span>LM</span><h1>Lead Magnet<br />CRM</h1><p>캠페인 유입부터 전환까지 한 곳에서 관리하세요.</p></div>
    <form className="card" onSubmit={login}><h2>운영자 로그인</h2><label>아이디<input name="username" defaultValue="admin" required /></label><label>비밀번호<input name="password" type="password" defaultValue="admin123" required /></label>{error && <p className="error">{error}</p>}<button>로그인</button><small>초기 계정: admin / admin123</small></form></main>
}
