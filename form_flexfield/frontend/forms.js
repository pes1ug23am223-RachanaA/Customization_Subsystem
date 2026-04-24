// forms.js — Form Designer v2
// Fix: inline form view (no redirect), duplicate field prevention,
//      delete individual fields, proper exception handling

const FORMS_API = 'http://localhost:8080/api/forms';

let forms = [];
let selectedFormId = null;

/* ── Utilities ── */
function normalizeFieldType(t) {
  const map = { Dropdown:'Select', Textarea:'Textarea', Flexfield:'Flexfield',
                Boolean:'Boolean', Number:'Number', Date:'Date' };
  return map[t] || t || 'Text';
}

function showToast(msg, type = 'success') {
  const t = document.getElementById('toast');
  if (!t) return;
  t.textContent = msg;
  t.className = 'toast ' + type;
  t.style.display = 'block';
  clearTimeout(t._timer);
  t._timer = setTimeout(() => { t.style.display = 'none'; }, 3500);
}

function setStatus(online) {
  const el = document.getElementById('serverStatus');
  if (!el) return;
  el.textContent = online ? 'Live (DB)' : 'Offline';
  el.className = online ? 'badge badge-green' : 'badge badge-red';
}

/* ── API ── */
async function loadForms() {
  try {
    const res = await fetch(FORMS_API);
    if (!res.ok) throw new Error('Server offline');
    const data = await res.json();
    forms = data.map(f => ({
      id: f.formId,
      name: f.formName,
      layout: f.layoutType || 'Grid',
      moduleId: f.moduleId || 0,
      fields: (f.fields || []).map(fld => ({
        id: fld.fieldId || fld.id,
        name: fld.fieldName,
        type: normalizeFieldType(fld.fieldType),
        required: fld.isMandatory === true || fld.isMandatory === 1
      }))
    }));
    if (!selectedFormId && forms.length) selectedFormId = forms[0].id;
    setStatus(true);
  } catch {
    forms = [];
    selectedFormId = null;
    setStatus(false);
  }
  renderAll();
}

/* ── Render ── */
function renderAll() {
  renderFormTable();
  renderFields();
  renderPreview();
  const f = forms.find(x => x.id === selectedFormId);
  document.getElementById('formCount').innerText = forms.length;
  document.getElementById('fieldCount').innerText = forms.reduce((s, f) => s + f.fields.length, 0);
  document.getElementById('formNameLabel').innerText = f ? `— ${f.name}` : '';
  document.getElementById('previewFormName').innerText = f ? f.name : 'No form selected';
}

function renderFormTable() {
  const tbody = document.getElementById('formTable');
  if (!tbody) return;
  if (!forms.length) {
    tbody.innerHTML = '<tr><td colspan="4" class="empty-state">No forms yet. Create one to get started.</td></tr>';
    return;
  }
  tbody.innerHTML = forms.map(f => `
    <tr onclick="selectForm(${f.id})"
        style="cursor:pointer;${selectedFormId===f.id ? 'background:var(--primary-light);' : ''}">
      <td>${f.id}</td>
      <td><strong>${f.name}</strong></td>
      <td>${f.layout}</td>
      <td>
        <button class="open-form-btn" onclick="openFormView(${f.id});event.stopPropagation()">
          Open Form
        </button>
      </td>
    </tr>`).join('');
}

function renderFields() {
  const tbody = document.getElementById('fieldTable');
  if (!tbody) return;
  const form = forms.find(f => f.id === selectedFormId);
  if (!form) {
    tbody.innerHTML = '<tr><td colspan="4" class="empty-state">Select a form to see its fields.</td></tr>';
    return;
  }
  if (!form.fields.length) {
    tbody.innerHTML = '<tr><td colspan="4" class="empty-state">No fields yet. Add a field or flexfield.</td></tr>';
    return;
  }
  tbody.innerHTML = form.fields.map(f => `
    <tr>
      <td>${f.name}</td>
      <td><span class="type-badge type-${f.type.toLowerCase()}">${f.type}</span></td>
      <td>${f.required ? '<span class="badge badge-blue">Yes</span>' : 'No'}</td>
      <td>
        <button class="field-del" onclick="deleteField(${f.id});event.stopPropagation()">
          Remove
        </button>
      </td>
    </tr>`).join('');
}

function renderPreview() {
  const pa = document.getElementById('previewArea');
  if (!pa) return;
  const form = forms.find(f => f.id === selectedFormId);
  if (!form || !form.fields.length) {
    pa.innerHTML = '<p class="empty-state">Select a form to preview its fields.</p>';
    return;
  }
  pa.innerHTML = form.fields.map(f => `
    <div class="form-field-render">
      <label>${f.name}${f.required ? '<span class="req-star">*</span>' : ''}</label>
      ${renderInput(f.type)}
    </div>`).join('');
}

function renderInput(type) {
  switch (type) {
    case 'Date':      return '<input type="date">';
    case 'Number':    return '<input type="number" placeholder="0">';
    case 'Boolean':   return '<label style="font-weight:normal"><input type="checkbox" style="width:auto;margin-right:6px"> Yes</label>';
    case 'Select':    return '<select><option>-- Select --</option><option>Option A</option><option>Option B</option></select>';
    case 'Textarea':  return '<textarea placeholder="Enter text..." rows="3"></textarea>';
    case 'Flexfield': return '<input type="text" placeholder="Flexfield composite value">';
    default:          return '<input type="text" placeholder="Enter value...">';
  }
}

/* ── Selection ── */
function selectForm(id) {
  selectedFormId = id;
  closeFormView();
  renderAll();
}

/* ── Open Form inline (no redirect) ── */
function openFormView(id) {
  selectedFormId = id;
  const form = forms.find(f => f.id === id);
  if (!form) return;

  const panel = document.getElementById('formViewPanel');
  const title = document.getElementById('formViewTitle');
  const body  = document.getElementById('formViewBody');

  title.textContent = form.name + ' — ' + form.layout + ' Layout';

  body.innerHTML = `
    <p style="font-size:13px;color:var(--gray-500);margin-bottom:18px">
      Fill in the form below. Required fields are marked with an asterisk.
    </p>
    <div id="formFields"></div>
    <div class="form-actions">
      <button class="close-form-btn" onclick="closeFormView()">Close</button>
      <button class="submit-form-btn" onclick="submitFormView()">Submit</button>
    </div>`;

  const container = body.querySelector('#formFields');
  container.innerHTML = form.fields.map(f => `
    <div class="form-field-render">
      <label>${f.name}${f.required ? '<span class="req-star">*</span>' : ''}</label>
      <div data-field="${f.name}" data-required="${f.required}">
        ${renderInput(f.type)}
      </div>
    </div>`).join('');

  panel.classList.add('active');
  panel.scrollIntoView({ behavior: 'smooth', block: 'start' });
  renderAll();
}

function closeFormView() {
  document.getElementById('formViewPanel').classList.remove('active');
}

function submitFormView() {
  const form = forms.find(f => f.id === selectedFormId);
  if (!form) return;
  // Validate required fields
  const requiredFields = form.fields.filter(f => f.required);
  const missing = requiredFields.filter(f => {
    const wrapper = document.querySelector(`[data-field="${f.name}"]`);
    if (!wrapper) return false;
    const input = wrapper.querySelector('input,select,textarea');
    if (!input) return false;
    if (input.type === 'checkbox') return false;
    return !input.value.trim();
  });
  if (missing.length) {
    showToast(`Required fields missing: ${missing.map(f=>f.name).join(', ')}`, 'error');
    return;
  }
  showToast(`Form "${form.name}" submitted successfully.`, 'success');
  closeFormView();
}

/* ── Create Form ── */
async function addForm() {
  const nameEl = document.getElementById('formName');
  const layoutEl = document.getElementById('layoutType');
  const name   = nameEl.value.trim();
  const layout = layoutEl ? layoutEl.value : 'Grid';
  if (!name) { showToast('Form name is required.', 'error'); return; }

  // Duplicate form name check
  if (forms.some(f => f.name.toLowerCase() === name.toLowerCase())) {
    showToast(`A form named "${name}" already exists.`, 'error');
    return;
  }

  try {
    const res = await fetch(`${FORMS_API}/create`, {
      method:'POST', headers:{'Content-Type':'application/json'},
      body: JSON.stringify({ formName: name, layoutType: layout })
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Unable to save form');
    selectedFormId = data.formId || data.id;
    showToast(`Form "${name}" created.`);
  } catch (err) {
    showToast(err.message || 'Unable to save form.', 'error');
    return;
  }
  nameEl.value = '';
  if (layoutEl) layoutEl.value = 'Grid';
  await loadForms();
}

/* ── Add Field with duplicate check ── */
async function addField() {
  if (!selectedFormId) { showToast('Select a form first.', 'error'); return; }

  const nameEl = document.getElementById('fieldName');
  const typeEl = document.getElementById('fieldType');
  const reqEl  = document.getElementById('requiredField');
  const name   = nameEl.value.trim();
  const type   = normalizeFieldType(typeEl.value);
  const req    = reqEl ? reqEl.checked : false;

  if (!name) { showToast('Field name is required.', 'error'); return; }

  // Duplicate field name check within this form
  const form = forms.find(f => f.id === selectedFormId);
  if (form && form.fields.some(f => f.name.toLowerCase() === name.toLowerCase())) {
    showToast(`Field "${name}" already exists in this form. Use a unique name.`, 'error');
    return;
  }

  try {
    const res = await fetch(`${FORMS_API}/${selectedFormId}/fields`, {
      method:'POST', headers:{'Content-Type':'application/json'},
      body: JSON.stringify({ fieldName: name, fieldType: type, isMandatory: req })
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Unable to add field');
    showToast(`Field "${name}" added.`);
  } catch (err) {
    showToast(err.message || 'Unable to add field.', 'error');
    return;
  }
  nameEl.value = '';
  typeEl.value = 'Text';
  if (reqEl) reqEl.checked = false;
  await loadForms();
}

/* ── Delete Field ── */
async function deleteField(fieldId) {
  if (!fieldId) { showToast('No field selected.', 'error'); return; }
  if (!confirm('Remove this field from the form?')) return;
  try {
    const res = await fetch(`${FORMS_API}/${selectedFormId}/fields/${fieldId}`, { method:'DELETE' });
    if (!res.ok) {
      const data = await res.json().catch(() => ({}));
      throw new Error(data.message || 'Unable to delete field');
    }
    showToast('Field removed.');
  } catch (err) {
    showToast(err.message || 'Unable to remove field.', 'error');
    return;
  }
  await loadForms();
}

/* ── Delete Form ── */
async function deleteForm() {
  if (!selectedFormId) { showToast('Select a form first.', 'error'); return; }
  const f = forms.find(x => x.id === selectedFormId);
  if (!confirm(`Delete form "${f ? f.name : selectedFormId}"? This will also remove all its fields.`)) return;
  try {
    const res = await fetch(`${FORMS_API}/${selectedFormId}`, { method:'DELETE' });
    if (!res.ok) {
      const data = await res.json().catch(() => ({}));
      throw new Error(data.message || 'Unable to delete form');
    }
    showToast('Form deleted.');
  } catch (err) {
    showToast(err.message || 'Unable to delete form.', 'error');
    return;
  }
  selectedFormId = null;
  closeFormView();
  await loadForms();
}

/* ── Attach Flexfield ── */
async function attachFlexfield() {
  const sel = document.getElementById('flexSelect');
  if (!sel || !sel.value || sel.value === 'No flexfields defined') {
    showToast('Select a flexfield.', 'error'); return;
  }
  const name = sel.value;

  // Duplicate check
  const form = forms.find(f => f.id === selectedFormId);
  if (form && form.fields.some(f => f.name.toLowerCase() === name.toLowerCase())) {
    showToast(`Flexfield "${name}" is already attached to this form.`, 'error');
    return;
  }

  try {
    const res = await fetch(`${FORMS_API}/${selectedFormId}/fields`, {
      method:'POST', headers:{'Content-Type':'application/json'},
      body: JSON.stringify({ fieldName: name, fieldType: 'Flexfield', isMandatory: false })
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Unable to attach flexfield');
    showToast(`Flexfield "${name}" attached.`);
  } catch (err) {
    showToast(err.message || 'Unable to attach flexfield.', 'error');
    return;
  }
  closeFlexModal();
  await loadForms();
}

/* ── Modal helpers ── */
function showModal(id) {
  const m = document.getElementById(id);
  if (m) { m.style.visibility='visible'; m.style.opacity='1'; m.style.pointerEvents='auto'; }
}
function hideModal(id) {
  const m = document.getElementById(id);
  if (m) { m.style.visibility='hidden'; m.style.opacity='0'; m.style.pointerEvents='none'; }
}

function openFormModal()  { document.getElementById('formName').value=''; showModal('formModal'); }
function closeFormModal() { hideModal('formModal'); }

function openFieldModal() {
  if (!selectedFormId) { showToast('Select a form first.', 'error'); return; }
  document.getElementById('fieldName').value = '';
  document.getElementById('fieldType').value = 'Text';
  document.getElementById('requiredField').checked = false;
  showModal('fieldModal');
}
function closeFieldModal() { hideModal('fieldModal'); }

function openFlexModal() {
  if (!selectedFormId) { showToast('Select a form first.', 'error'); return; }
  loadFlexSelect();
  showModal('flexModal');
}
function closeFlexModal() { hideModal('flexModal'); }

function loadFlexSelect() {
  const sel = document.getElementById('flexSelect');
  if (!sel) return;
  sel.innerHTML = '<option>Loading...</option>';
  fetch('http://localhost:8080/api/flexfields')
    .then(r => r.json())
    .then(data => {
      if (!data.length) {
        sel.innerHTML = '<option>No flexfields defined — create one first</option>';
      } else {
        sel.innerHTML = data.map(f =>
          `<option value="${f.name||f.fieldName||''}">${f.name||f.fieldName||'Unnamed'} (${f.type||''})</option>`
        ).join('');
      }
    })
    .catch(() => { sel.innerHTML = '<option>Offline — server required</option>'; });
}

/* ── Form save helpers (called from modal) ── */
async function saveForm()  { await addForm();  closeFormModal(); }
async function saveField() { await addField(); closeFieldModal(); }

/* ── Close modals on backdrop click ── */
document.querySelectorAll('.modal').forEach(m => {
  m.addEventListener('click', e => { if (e.target === m) { m.style.visibility='hidden'; m.style.opacity='0'; m.style.pointerEvents='none'; } });
});

/* ── Init ── */
loadForms();
