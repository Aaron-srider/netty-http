import Vue from 'vue';
import Vuex from 'vuex';
import development from './modules/development';
Vue.use(Vuex);

export default new Vuex.Store({
    state: {},
    getters: {
        debugView: (state) => {
            // @ts-ignore
            return state.development.debugView;
        },
        logEnabled: (state) => {
            // @ts-ignore
            return state.development.logEnabled;
        },
    },
    mutations: {},
    actions: {},
    modules: {
        development,
    },
});
