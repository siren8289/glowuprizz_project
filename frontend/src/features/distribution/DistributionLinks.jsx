import { api, publicBaseUrl } from '../../shared/api'

const channels = ['INSTAGRAM', 'X', 'YOUTUBE', 'THREADS']

export function DistributionLinks({ token, campaign, onComplete, onError }) {
  const createLink = async (event) => {
    event.preventDefault()
    try {
      await api(token, `/api/campaigns/${campaign.id}/distribution-links`, { method: 'POST', body: JSON.stringify({ channel: new FormData(event.currentTarget).get('channel') }) })
      onComplete()
    } catch (exception) { onError(exception.message) }
  }
  return <><div className="links"><h3>배포 링크</h3>{campaign.distributionLinks.map(link => <div className="link" key={link.id}><span>{link.channel}</span><code>{`${publicBaseUrl}${link.url}`}</code><button className="copy" type="button" onClick={() => navigator.clipboard.writeText(`${publicBaseUrl}${link.url}`)}>복사</button></div>)}{!campaign.distributionLinks.length && <p>생성된 배포 링크가 없습니다.</p>}</div>
    <form className="link-form" onSubmit={createLink}><select name="channel">{channels.map(channel => <option key={channel}>{channel}</option>)}</select><button>채널 링크 생성</button></form></>
}
