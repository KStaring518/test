import { api } from './index'

export interface CreateReviewPayload {
  orderItemId: number
  rating: number
  content?: string
  images?: string
  anonymous?: boolean
}

export const createReview = (payload: CreateReviewPayload) => {
  const form = new FormData()
  form.append('orderItemId', String(payload.orderItemId))
  form.append('rating', String(payload.rating))
  if (payload.content) form.append('content', payload.content)
  if (payload.images) form.append('images', payload.images)
  if (typeof payload.anonymous === 'boolean') form.append('anonymous', String(payload.anonymous))
  return api.post('/reviews/create', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const listReviewsByProduct = (productId: number) => {
  return api.get(`/reviews/public/product/${productId}`)
}


