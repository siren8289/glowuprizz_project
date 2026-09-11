export function TemplateList({ forms, selectedTemplate, onSelect }) {
  return <section className="panel templates"><div><h2>등록된 폼 템플릿</h2><p>템플릿을 선택하면 캠페인 생성에 바로 적용됩니다.</p></div>
    <div className="template-grid">{forms.map(form => <button type="button" className={`template-card ${String(form.id) === selectedTemplate ? 'selected' : ''}`} key={form.id} onClick={() => onSelect(String(form.id))}>
      <span>HTML TEMPLATE</span><strong>{form.name}</strong><small>{new Date(form.createdAt).toLocaleDateString('ko-KR')} 등록</small>
    </button>)}</div>
  </section>
}
