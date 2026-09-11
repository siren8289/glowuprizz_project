import { api } from '../../shared/api'

export function FormUpload({ token, onComplete, onError }) {
  const upload = async (event) => {
    event.preventDefault()
    const form = event.currentTarget
    const data = new FormData(form)
    if (!data.get('file')?.name) return onError('HTML 파일을 선택하세요.')
    try { await api(token, '/api/forms', { method: 'POST', body: data }); form.reset(); onComplete() }
    catch (exception) { onError(exception.message) }
  }
  return <form className="panel" onSubmit={upload}><h2>1. HTML 폼 등록</h2><p>AI로 생성한 단일 HTML 파일을 업로드하세요.</p><input name="file" type="file" accept=".html,text/html" required /><button>템플릿 등록</button></form>
}
