import Login from '../components/Login.vue'
import NotFound from '../components/404.vue'
import Visitor from '../components/visitor.vue'
import Home from '../components/home.vue'

//system
import System from "../components/system.vue"
import CustomerManage from '../components/system/customer_manage.vue'

export default
[
    {
        path: '/login',
        component: Login,
        name: 'Login',
        hidden: true
    },
    {
        path: '/404',
        component: NotFound,
        name: '404',
        hidden: true
    },
    {
        path: '/visitor',
        component: Visitor,
        name: 'visitor',
        hidden: true
    },
    {
        path: '/home',
        component: Home,
        redirect: '/home/system',
        children: [
            {
                path: '/home/system',
                component: System,
                redirect: '/home/system/customer_manage',
                name: 'System',
                meta: "系统管理",
                children: [
                    {
                        path: '/home/system/customer_manage',
                        component: CustomerManage,
                        name: 'customer_manage',
                        meta: '访客管理'
                    }
                ]
            },
        ]
    },
    {
        path: '*',
        redirect: '/home',
        hidden: true
    },
]
