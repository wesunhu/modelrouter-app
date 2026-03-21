/**
 * CRUD UI for models.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { api, Model, Provider } from '../api/client'
import { Button, Table, TableBody, TableCell, TableHead, TableRow, Dialog, TextField, DialogTitle, DialogContent, DialogActions, IconButton, MenuItem } from '@mui/material'
import DeleteIcon from '@mui/icons-material/Delete'
import EditIcon from '@mui/icons-material/Edit'

export default function Models() {
  const { t } = useTranslation()
  const [list, setList] = useState<Model[]>([])
  const [providers, setProviders] = useState<Provider[]>([])
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<Model | null>(null)
  const [form, setForm] = useState({
    name: '', modelId: '', modelType: 'text', contextWindow: 4096, maxTokens: 2048,
    costInput: 0, costOutput: 0, tokenCost: 0 as number | '', status: 'active',
    provider: { id: 0, name: '' } as { id?: number; name: string },
  })

  const load = () => {
    api.get<Model[]>('/api/models').then(r => setList(r.data || []))
    api.get<Provider[]>('/api/providers').then(r => setProviders(r.data || []))
  }

  useEffect(() => { load() }, [])

  const save = async () => {
    if (!form.name?.trim() || !form.modelId?.trim()) {
      alert(t('models.fillRequired'))
      return
    }
    const providerPayload = (form.provider?.id || form.provider?.name?.trim())
      ? { id: form.provider?.id, name: form.provider?.name || '' }
      : null
    if (!providerPayload) {
      alert(t('models.selectProvider'))
      return
    }
    const payload = {
      name: form.name.trim(),
      modelId: form.modelId.trim(),
      modelType: form.modelType || 'text',
      contextWindow: form.contextWindow ?? 4096,
      maxTokens: form.maxTokens ?? 2048,
      costInput: form.costInput ?? 0,
      costOutput: form.costOutput ?? 0,
      tokenCost: form.tokenCost === '' ? null : Number(form.tokenCost),
      status: form.status || 'active',
      provider: providerPayload,
    }
    try {
      if (editing) {
        await api.put(`/api/models/${editing.id}`, payload)
      } else {
        await api.post('/api/models', payload)
      }
      setOpen(false)
      setEditing(null)
      load()
    } catch (e: unknown) {
      const err = e as { parsedMessage?: string; response?: { status?: number }; message?: string }
      const msg = err.parsedMessage || (err.response ? `HTTP ${err.response.status}` : '') || err.message || String(e)
      alert(t('models.saveFailed') + ': ' + msg)
    }
  }

  const del = async (id: number) => {
    if (confirm(t('common.confirmDelete'))) {
      await api.delete(`/api/models/${id}`)
      load()
    }
  }

  const edit = (m: Model) => {
    setEditing(m)
    setForm({
      name: m.name, modelId: m.modelId, modelType: m.modelType || 'text',
      contextWindow: m.contextWindow || 4096, maxTokens: m.maxTokens || 2048,
      costInput: m.costInput ?? 0, costOutput: m.costOutput ?? 0,
      tokenCost: m.tokenCost ?? '',
      status: m.status || 'active',
      provider: m.provider ? { id: m.provider.id, name: m.provider.name } : { name: '' },
    })
    setOpen(true)
  }

  return (
    <div>
      <Button variant="contained" onClick={() => { setEditing(null); setForm({ name: '', modelId: '', modelType: 'text', contextWindow: 4096, maxTokens: 2048, costInput: 0, costOutput: 0, tokenCost: '', status: 'active', provider: { name: '' } }); setOpen(true) }}>{t('models.addModel')}</Button>
      <Table sx={{ mt: 2 }}>
        <TableHead><TableRow><TableCell>{t('common.name')}</TableCell><TableCell>{t('models.modelId')}</TableCell><TableCell>{t('models.provider')}</TableCell><TableCell>{t('models.type')}</TableCell><TableCell>{t('common.actions')}</TableCell></TableRow></TableHead>
        <TableBody>
          {list.map(m => (
            <TableRow key={m.id}>
              <TableCell>{m.name}</TableCell>
              <TableCell>{m.modelId}</TableCell>
              <TableCell>{m.provider?.name}</TableCell>
              <TableCell>{m.modelType}</TableCell>
              <TableCell>
                <IconButton size="small" onClick={() => edit(m)}><EditIcon /></IconButton>
                <IconButton size="small" onClick={() => del(m.id)}><DeleteIcon /></IconButton>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editing ? t('models.editModel') : t('models.addModelTitle')}</DialogTitle>
        <DialogContent>
          <TextField fullWidth label={t('common.name')} value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} sx={{ mt: 1 }} />
          <TextField fullWidth label={t('models.modelId')} value={form.modelId} onChange={e => setForm({ ...form, modelId: e.target.value })} sx={{ mt: 1 }} placeholder={t('models.modelIdPlaceholder')} />
          <TextField select fullWidth label={t('models.provider')} value={String(form.provider?.id ?? form.provider?.name ?? '')} onChange={e => {
            const v = e.target.value
            const p = providers.find(x => String(x.id) === String(v) || x.name === v)
            setForm({ ...form, provider: p ? { id: p.id, name: p.name } : { id: undefined, name: String(v || '') } })
          }} sx={{ mt: 1 }} helperText={!providers.length ? t('models.providerHelper') : ''}>
            <MenuItem value="">{t('models.providerPlaceholder')}</MenuItem>
            {providers.map(p => <MenuItem key={p.id} value={String(p.id)}>{p.name}</MenuItem>)}
          </TextField>
          <TextField fullWidth label={t('models.type')} value={form.modelType} onChange={e => setForm({ ...form, modelType: e.target.value })} sx={{ mt: 1 }} />
          <TextField type="number" fullWidth label={t('models.tokenCost')} value={form.tokenCost} onChange={e => setForm({ ...form, tokenCost: e.target.value === '' ? '' : Number(e.target.value) })} sx={{ mt: 1 }} inputProps={{ step: 0.0001, min: 0 }} placeholder="0.0001" helperText={t('models.tokenCostHelper')} />
        </DialogContent>
        <DialogActions><Button onClick={() => setOpen(false)}>{t('common.cancel')}</Button><Button variant="contained" onClick={save}>{t('common.save')}</Button></DialogActions>
      </Dialog>
    </div>
  )
}
