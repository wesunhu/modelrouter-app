import { Component, ErrorInfo, ReactNode } from 'react'

interface Props { children: ReactNode }
interface State { hasError: boolean; error?: Error }

export class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false }

  static getDerivedStateFromError(error: Error) {
    return { hasError: true, error }
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('ErrorBoundary:', error, info)
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{ padding: 24, fontFamily: 'sans-serif' }}>
          <h2 style={{ color: '#c62828' }}>页面加载失败</h2>
          <pre style={{ background: '#f5f5f5', padding: 16, overflow: 'auto' }}>
            {this.state.error?.message}
          </pre>
          <p>请检查控制台 (F12) 获取更多信息</p>
        </div>
      )
    }
    return this.props.children
  }
}
