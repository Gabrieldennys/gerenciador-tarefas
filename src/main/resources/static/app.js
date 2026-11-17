const $ = s => document.querySelector(s);
const state = { tasks: [], people: [], categories: [] };
const labels = { PENDENTE: 'Pendente', EM_ANDAMENTO: 'Em andamento', CONCLUIDA: 'Concluída' };
const esc = value => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
async function api(path, options = {}) {
  let response;
  try { response = await fetch(path, { ...options, headers: { 'Content-Type': 'application/json', ...options.headers } }); }
  catch { throw new Error('Não foi possível conectar ao servidor. Verifique se a aplicação está em execução.'); }
  if (!response.ok) { let error = {}; try { error = await response.json(); } catch {} throw new Error(error.detalhes ? Object.values(error.detalhes).join(' · ') : error.mensagem || 'Não foi possível concluir a operação.'); }
  return response.status === 204 ? null : response.json();
}
function toast(message) { const el = $('#toast'); el.textContent = message; el.classList.add('show'); clearTimeout(toast.timer); toast.timer = setTimeout(() => el.classList.remove('show'), 3500); }
async function refresh() {
  try {
    const [people, categories] = await Promise.all([api('/usuarios'), api('/categorias')]);
    const first = await api('/tarefas?size=100&page=0');
    const pages = await Promise.all(Array.from({length: Math.max(0, first.totalPages - 1)}, (_, i) => api(`/tarefas?size=100&page=${i + 1}`)));
    Object.assign(state, { people, categories, tasks: [first, ...pages].flatMap(p => p.content) });
    render();
  } catch (e) { $('#tasks').innerHTML = `<div class="empty"><strong>Não foi possível carregar as tarefas</strong>${esc(e.message)}</div>`; toast(e.message); }
}
function render() {
  const counts = status => state.tasks.filter(t => t.status === status).length;
  $('#total').textContent = state.tasks.length; $('#pending').textContent = counts('PENDENTE'); $('#progress').textContent = counts('EM_ANDAMENTO'); $('#done').textContent = counts('CONCLUIDA');
  const search = $('#search').value.toLocaleLowerCase('pt-BR').trim(), filter = $('#filter').value;
  const tasks = state.tasks.filter(t => (!filter || t.status === filter) && `${t.titulo} ${t.descricao || ''} ${t.categoria || ''}`.toLocaleLowerCase('pt-BR').includes(search));
  $('#task-count').textContent = tasks.length;
  $('#tasks').innerHTML = tasks.length ? tasks.map(t => `<div class="task-row"><div><div class="task-title">${esc(t.titulo)}</div><div class="task-desc">${esc(t.categoria || t.descricao || 'Sem categoria')}</div></div><div><span class="badge ${esc(t.status)}">${esc(labels[t.status] || t.status)}</span></div><div class="muted">${t.dataLimite ? esc(t.dataLimite.split('-').reverse().join('/')) : '—'}</div><div class="muted">${esc(state.people.find(p => p.id === t.usuarioId)?.nome || '—')}</div><div class="row-actions">${t.status !== 'CONCLUIDA' ? `<button class="icon-btn" title="Alterar status" aria-label="Alterar status" data-action="status" data-id="${t.id}">↗</button><button class="icon-btn" title="Editar tarefa" aria-label="Editar tarefa" data-action="edit" data-id="${t.id}">✎</button>` : ''}<button class="icon-btn" title="Excluir tarefa" aria-label="Excluir tarefa" data-action="delete" data-id="${t.id}">×</button></div></div>`).join('') : '<div class="empty"><span class="empty-icon">✳</span><strong>Nenhuma tarefa por aqui</strong>Crie uma tarefa ou ajuste os filtros para começar.</div>';
  $('#people-list').innerHTML = state.people.length ? state.people.map(p => `<div class="simple-item"><div><strong>${esc(p.nome)}</strong><small>${esc(p.email)}</small></div><button class="icon-btn" data-kind="usuarios" data-id="${p.id}" aria-label="Excluir pessoa">×</button></div>`).join('') : '<div class="empty"><strong>Nenhuma pessoa cadastrada</strong>Adicione uma pessoa para atribuir tarefas.</div>';
  $('#categories-list').innerHTML = state.categories.length ? state.categories.map(c => `<div class="simple-item"><strong>${esc(c.nome)}</strong><button class="icon-btn" data-kind="categorias" data-id="${c.id}" aria-label="Excluir categoria">×</button></div>`).join('') : '<div class="empty"><strong>Nenhuma categoria cadastrada</strong>Adicione categorias para organizar suas tarefas.</div>';
}
function openForm(type, item) {
  const form = $('#form'); form.reset(); $('#form-error').textContent = '';
  const field = (label, control) => `<label class="field">${label}${control}</label>`;
  if (type === 'task' && !state.people.length) { toast('Cadastre uma pessoa antes de criar uma tarefa.'); return; }
  $('#modal-title').textContent = type === 'task' ? (item ? 'Editar tarefa' : 'Nova tarefa') : type === 'person' ? 'Nova pessoa' : type === 'category' ? 'Nova categoria' : 'Alterar status';
  let fields = '';
  if (type === 'task') {
    const categoryId = state.categories.find(c => c.nome === item?.categoria)?.id;
    fields += field('Título *', `<input name="titulo" maxlength="100" required value="${esc(item?.titulo)}" placeholder="O que precisa ser feito?">`);
    fields += field('Descrição', `<textarea name="descricao" maxlength="500" placeholder="Adicione detalhes, se quiser">${esc(item?.descricao)}</textarea>`);
    fields += field('Responsável *', `<select name="usuarioId" required><option value="">Selecione uma pessoa</option>${state.people.map(p => `<option value="${p.id}" ${p.id === item?.usuarioId ? 'selected' : ''}>${esc(p.nome)}</option>`).join('')}</select>`);
    fields += field('Categoria', `<select name="categoriaId"><option value="">Sem categoria</option>${state.categories.map(c => `<option value="${c.id}" ${c.id === categoryId ? 'selected' : ''}>${esc(c.nome)}</option>`).join('')}</select>`);
    fields += field('Prazo', `<input type="date" name="dataLimite" value="${esc(item?.dataLimite)}">`);
  } else if (type === 'person') fields = field('Nome *', '<input name="nome" required>') + field('E-mail *', '<input name="email" type="email" required>');
  else if (type === 'category') fields = field('Nome *', '<input name="nome" required>');
  else fields = field('Status', `<select name="status">${Object.entries(labels).map(([value,label]) => `<option value="${value}" ${value === item.status ? 'selected' : ''}>${label}</option>`).join('')}</select><small>Uma tarefa concluída não poderá ser editada ou reaberta.</small>`);
  $('#fields').innerHTML = fields; $('#modal').showModal();
  form.onsubmit = async event => {
    event.preventDefault(); const data = Object.fromEntries(new FormData(form));
    try {
      if (type === 'task') { data.usuarioId = Number(data.usuarioId); data.categoriaId = data.categoriaId ? Number(data.categoriaId) : null; data.dataLimite ||= null; await api(item ? `/tarefas/${item.id}` : '/tarefas', {method: item ? 'PUT' : 'POST', body: JSON.stringify(data)}); }
      else if (type === 'status') await api(`/tarefas/${item.id}/status?novo=${encodeURIComponent(data.status)}`, { method: 'PATCH' });
      else await api(type === 'person' ? '/usuarios' : '/categorias', {method:'POST', body:JSON.stringify(data)});
      $('#modal').close(); await refresh(); toast('Salvo com sucesso.');
    } catch (e) { $('#form-error').textContent = e.message; }
  };
}
$('.sidebar nav').addEventListener('click', e => { const button = e.target.closest('[data-view]'); if (!button) return; document.querySelectorAll('.nav').forEach(n => n.classList.toggle('active', n === button)); ['tarefas','pessoas','categorias'].forEach(v => $(`#${v}-view`).hidden = v !== button.dataset.view); $('#breadcrumb').textContent = button.textContent.trim().toLocaleUpperCase('pt-BR'); });
$('#new-task').onclick = () => openForm('task'); $('#new-person').onclick = () => openForm('person'); $('#new-category').onclick = () => openForm('category');
document.querySelectorAll('.close').forEach(b => b.onclick = () => $('#modal').close());
$('#search').oninput = render; $('#filter').onchange = render;
$('#tasks').onclick = async e => { const b = e.target.closest('[data-action]'); if (!b) return; const task = state.tasks.find(t => t.id === Number(b.dataset.id)); if (!task) return; if (b.dataset.action === 'edit') openForm('task', task); else if (b.dataset.action === 'status') openForm('status', task); else if (confirm(`Excluir a tarefa “${task.titulo}”?`)) { try { await api(`/tarefas/${task.id}`, {method:'DELETE'}); await refresh(); toast('Tarefa excluída.'); } catch (error) { toast(error.message); } } };
for (const id of ['people-list','categories-list']) $(`#${id}`).onclick = async e => { const b = e.target.closest('[data-kind]'); if (!b || !confirm('Deseja excluir este registro?')) return; try { await api(`/${b.dataset.kind}/${b.dataset.id}`, {method:'DELETE'}); await refresh(); toast('Registro excluído.'); } catch (error) { toast(error.message); } };
refresh();
