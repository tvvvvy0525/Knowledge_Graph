import request from '@/utils/request'

/**
 * 问答接口
 * @param {string} question 用户的问题
 */
export function askAI(question) {
  return request({
    url: '/api/chat/ask',
    method: 'get',
    params: { question }
  })
}