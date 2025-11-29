import request from '@/utils/request'

export function assessRisk(data) {
    return request({
        url: '/api/diagnosis/assess',
        method: 'post',
        data
    })
}