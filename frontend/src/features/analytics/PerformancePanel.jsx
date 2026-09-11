import { useCallback, useEffect, useState } from 'react'
import { api } from '../../shared/api'
import { DistributionLinks } from '../distribution/DistributionLinks'

const METRICS_REFRESH_MS = 5000

export function PerformancePanel({ token, campaigns, selectedCampaign, onSelect, reload, onError }) {
  const [stats, setStats] = useState(null)
  const [channelStats, setChannelStats] = useState([])
  const campaign = campaigns.find(item => String(item.id) === selectedCampaign)
  const refreshMetrics = useCallback(async () => {
    if (!selectedCampaign) {
      setStats(null)
      setChannelStats([])
      return
    }
    try {
      const [overall, byChannel] = await Promise.all([api(token, `/api/campaigns/${selectedCampaign}/statistics`), api(token, `/api/campaigns/${selectedCampaign}/statistics/channels`)])
      setStats(overall); setChannelStats(byChannel)
    } catch (exception) { onError(exception.message) }
  }, [onError, selectedCampaign, token])
  useEffect(() => {
    refreshMetrics()
    const intervalId = window.setInterval(refreshMetrics, METRICS_REFRESH_MS)
    return () => window.clearInterval(intervalId)
  }, [refreshMetrics])
  const linkCreated = async () => { await reload(); await refreshMetrics() }
  return <section className="panel performance"><div className="section-head"><div><h2>캠페인 성과</h2><p>방문과 신청 데이터를 실시간으로 집계합니다.</p></div><select value={selectedCampaign} onChange={event => onSelect(event.target.value)}><option value="">캠페인 선택</option>{campaigns.map(item => <option key={item.id} value={item.id}>{item.name}</option>)}</select></div>
    {stats && <div className="metrics"><Metric label="전체 방문" value={stats.visits} /><Metric label="순 방문자" value={stats.uniqueVisitors} /><Metric label="신청 수" value={stats.applications} /><Metric label="전환율" value={`${stats.conversionRate}%`} /></div>}
    {campaign && <DistributionLinks token={token} campaign={campaign} onComplete={linkCreated} onError={onError} />}
    {channelStats.length > 0 && <table><thead><tr><th>채널</th><th>방문</th><th>순 방문자</th><th>신청</th><th>전환율</th></tr></thead><tbody>{channelStats.map(row => <tr key={row.token}><td>{row.channel}</td><td>{row.statistics.visits}</td><td>{row.statistics.uniqueVisitors}</td><td>{row.statistics.applications}</td><td>{row.statistics.conversionRate}%</td></tr>)}</tbody></table>}
  </section>
}
function Metric({ label, value }) { return <div><span>{label}</span><strong>{value}</strong></div> }
