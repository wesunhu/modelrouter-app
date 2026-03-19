import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { api, Provider } from '../api/client'
import { Button, Table, TableBody, TableCell, TableHead, TableRow, Dialog, TextField, DialogTitle, DialogContent, DialogActions, IconButton } from '@mui/material'
import DeleteIcon from '@mui/icons-material/Delete'
import EditIcon from '@mui/icons-material/Edit'
import OpenInNewIcon from '@mui/icons-material/OpenInNew'

export default function Providers() {
  const { t } = useTranslation()
  const [list, setList] = useState<Provider[]>([])
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<Provider | null>(null)
  const [form, setForm] = useState({ name: '', baseUrl: '', apiType: 'openai', authHeader: true, apiKey: '', registerUrl: '' })

  const load = () => api.get<Provider[]>('/api/providers').then(r => setList(r.data || []))

  useEffect(() => { load() }, [])

  const save = async () => {
    if (editing) {
      await api.put(`/api/providers/${editing.id}`, form)
    } else {
      await api.post('/api/providers', form)
    }
    setOpen(false)
    setEditing(null)
    setForm({ name: '', baseUrl: '', apiType: 'openai', authHeader: true, apiKey: '', registerUrl: '' })
    load()
  }

  const del = async (id: number) => {
    if (confirm(t('common.confirmDelete'))) {
      await api.delete(`/api/providers/${id}`)
      load()
    }
  }

  const edit = (p: Provider) => {
    setEditing(p)
    setForm({ name: p.name, baseUrl: p.baseUrl, apiType: p.apiType || 'openai', authHeader: p.authHeader ?? true, apiKey: p.apiKey || '', registerUrl: p.registerUrl || '' })
    setOpen(true)
  }

  return (
    <div>
      <Button variant="contained" onClick={() => { setEditing(null); setForm({ name: '', baseUrl: '', apiType: 'openai', authHeader: true, apiKey: '', registerUrl: '' }); setOpen(true) }}>{t('providers.addProvider')}</Button>
      <Table sx={{ mt: 2 }}>
        <TableHead><TableRow><TableCell>{t('common.name')}</TableCell><TableCell>{t('common.baseUrl')}</TableCell><TableCell>{t('common.apiType')}</TableCell><TableCell>{t('common.apiKey')}</TableCell><TableCell>{t('common.register')}</TableCell><TableCell>{t('common.actions')}</TableCell></TableRow></TableHead>
        <TableBody>
          {list.map(p => (
            <TableRow key={p.id}>
              <TableCell>{p.name}</TableCell>
              <TableCell>{p.baseUrl}</TableCell>
              <TableCell>{p.apiType}</TableCell>
              <TableCell>{p.apiKey ? p.apiKey.substring(0, 8) + '***' : '-'}</TableCell>
              <TableCell>
                {p.registerUrl ? (
                  <Button size="small" variant="outlined" startIcon={<OpenInNewIcon />} href={p.registerUrl} target="_blank" rel="noopener noreferrer">{t('common.register')}</Button>
                ) : '-'}
              </TableCell>
              <TableCell>
                <IconButton size="small" onClick={() => edit(p)}><EditIcon /></IconButton>
                <IconButton size="small" onClick={() => del(p.id)}><DeleteIcon /></IconButton>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
      <Dialog open={open} onClose={() => setOpen(false)}>
        <DialogTitle>{editing ? t('providers.editProvider') : t('providers.addProviderTitle')}</DialogTitle>
        <DialogContent>
          <TextField fullWidth label={t('common.name')} value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} sx={{ mt: 1 }} placeholder={t('providers.namePlaceholder')} inputProps={{ tabIndex: 1 }} />
          <TextField fullWidth label={t('common.baseUrl')} value={form.baseUrl} onChange={e => setForm({ ...form, baseUrl: e.target.value })} sx={{ mt: 1 }} placeholder={t('providers.baseUrlPlaceholder')} inputProps={{ tabIndex: 2 }} />
          <TextField fullWidth label={t('common.apiType')} value={form.apiType} onChange={e => setForm({ ...form, apiType: e.target.value })} sx={{ mt: 1 }} placeholder={t('providers.apiTypePlaceholder')} inputProps={{ tabIndex: 3 }} />
          <TextField fullWidth label={t('common.apiKey')} type="password" value={form.apiKey} onChange={e => setForm({ ...form, apiKey: e.target.value })} sx={{ mt: 1 }} placeholder={t('providers.apiKeyPlaceholder')} helperText={t('providers.apiKeyHelper')} inputProps={{ tabIndex: 4 }} autoComplete="off" />
          <TextField fullWidth label={t('common.registerUrl')} value={form.registerUrl} onChange={e => setForm({ ...form, registerUrl: e.target.value })} sx={{ mt: 1 }} placeholder={t('providers.registerUrlPlaceholder')} helperText={t('providers.registerUrlHelper')} inputProps={{ tabIndex: 5 }} />
        </DialogContent>
        <DialogActions><Button onClick={() => setOpen(false)}>{t('common.cancel')}</Button><Button variant="contained" onClick={save}>{t('common.save')}</Button></DialogActions>
      </Dialog>
    </div>
  )
}
