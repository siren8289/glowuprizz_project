import { useEffect, useState } from 'react'
import { createRoot } from 'react-dom/client'
import { api } from './shared/api'
import { LoginPage } from './features/auth/LoginPage'
import { FormUpload } from './features/forms/FormUpload'
import { TemplateList } from './features/forms/TemplateList'
import { CampaignCreate } from './features/campaigns/CampaignCreate'
import { PerformancePanel } from './features/analytics/PerformancePanel'
import './styles.css'

function App() {
  const [token, setToken] = useState(localStorage.getItem('lm_token'))
  const [forms, setForms] = useState([])
  const [campaigns, setCampaigns] = useState([])
  const [selectedCampaign, setSelectedCampaign] = useState('')
  const [selectedTemplate, setSelectedTemplate] = useState('')
  const [error, setError] = useState('')
  const load = async () => {
    try {
      const [templateList, campaignList] = await Promise.all([api(token, '/api/forms'), api(token, '/api/campaigns')])
      setForms(templateList); setCampaigns(campaignList)
      if (!selectedCampaign && campaignList[0]) setSelectedCampaign(String(campaignList[0].id))
    } catch (exception) { setError(exception.message) }
  }
  useEffect(() => { if (token) load() }, [token])
  if (!token) return <LoginPage onLogin={setToken} />
  const completedCampaign = async (id) => { await load(); setSelectedCampaign(String(id)) }
  return <main className="dashboard"><header><div><b>LM</b><strong>Lead Magnet CRM</strong></div><button className="secondary" onClick={() => { localStorage.removeItem('lm_token'); setToken(null) }}>로그아웃</button></header>
    <section className="hero"><p>OPERATIONS DASHBOARD</p><h1>전환을 만드는 캠페인 운영</h1><span>폼 등록부터 채널별 성과 분석까지, 리드 운영의 전체 흐름을 확인하세요.</span></section>
    {error && <p className="error">{error}</p>}
    <div className="grid"><FormUpload token={token} onComplete={load} onError={setError} /><CampaignCreate token={token} forms={forms} selectedTemplate={selectedTemplate} onSelectTemplate={setSelectedTemplate} onComplete={completedCampaign} onError={setError} /></div>
    <TemplateList forms={forms} selectedTemplate={selectedTemplate} onSelect={setSelectedTemplate} />
    <PerformancePanel token={token} campaigns={campaigns} selectedCampaign={selectedCampaign} onSelect={setSelectedCampaign} reload={load} onError={setError} />
  </main>
}
createRoot(document.getElementById('root')).render(<App />)
