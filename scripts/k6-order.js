import http from 'k6/http';
import { check } from 'k6';
export const options={scenarios:{create_order:{executor:'constant-arrival-rate',rate:Number(__ENV.RATE||10),timeUnit:'1s',duration:__ENV.DURATION||'30s',preAllocatedVUs:20,maxVUs:100}},thresholds:{http_req_failed:['rate<0.01'],http_req_duration:['p(95)<1000']}};
export default function(){const body=JSON.stringify({items:[{skuId:10001,quantity:1}]});const params={headers:{'Content-Type':'application/json','X-User-Id':'10001','X-Idempotency-Token':`${__VU}-${__ITER}-${Date.now()}`}};const r=http.post(`${__ENV.BASE_URL||'http://localhost:8080'}/api/orders`,body,params);check(r,{'created or stock exhausted':x=>x.status===200||x.status===400});}
