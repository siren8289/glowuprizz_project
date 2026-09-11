import { api } from '../../shared/api'

export function CampaignCreate({ token, forms, selectedTemplate, onSelectTemplate, onComplete, onError }) {
  const create = async (event) => {
    event.preventDefault()
    const form = event.currentTarget
    const payload = Object.fromEntries(new FormData(form))
    payload.templateId = Number(payload.templateId)
    try { const campaign = await api(token, '/api/campaigns', { method: 'POST', body: JSON.stringify(payload) }); form.reset(); onComplete(campaign.id) }
    catch (exception) { onError(exception.message) }
  }
  return <form className="panel" onSubmit={create}><h2>2. 캠페인 생성</h2><input name="name" placeholder="캠페인 이름" required /><select name="templateId" required value={selectedTemplate} onChange={event => onSelectTemplate(event.target.value)}><option value="" disabled>템플릿 선택</option>{forms.map(form => <option key={form.id} value={form.id}>{form.name}</option>)}</select><button disabled={!forms.length}>캠페인 만들기</button></form>
}
