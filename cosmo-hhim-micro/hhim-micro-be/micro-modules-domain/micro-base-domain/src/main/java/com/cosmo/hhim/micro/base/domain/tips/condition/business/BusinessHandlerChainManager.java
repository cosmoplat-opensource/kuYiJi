/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.business;

import com.cosmo.hhim.micro.base.domain.entity.tips.MicroContentTipConfig;
import com.google.common.collect.Lists;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/7
 */
public class BusinessHandlerChainManager implements IBusinessHandlerChain {

    private final ApplicationContext applicationContext;
    private final List<IBusinessHandler> handlers = Lists.newArrayList();

    private int currentPosition = 0;

    public BusinessHandlerChainManager(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 添加处理器到处理器链
     *
     * @param tipConfig
     * @return
     */
    public synchronized BusinessHandlerChainManager addHandler(MicroContentTipConfig tipConfig) {
        AbstractBusinessHandler businessHandler = applicationContext.getBean(tipConfig.getTriggerBusinessBean(), AbstractBusinessHandler.class);
        businessHandler.setOrder(tipConfig.getPriority().intValue());
        businessHandler.setTipConfig(tipConfig);

        // 根据order大小顺序插入
        int i = 0;
        while (i < this.handlers.size()) {
            IBusinessHandler currentValue = this.handlers.get(i);
            if (businessHandler.getOrder() >= currentValue.getOrder() && i < this.handlers.size()) {
                i++;
            } else {
                this.handlers.add(i, businessHandler);
                break;
            }
        }

        if (i == this.handlers.size()) {
            this.handlers.add(i, businessHandler);
        }
        return this;
    }

    @Override
    public BusinessHandlerResult executeNextHandler(BusinessHandlerParam param) {
//        return new VirtualHandlerChain(this.handlers).executeNextHandler(param); 

        if (this.currentPosition != this.handlers.size()) {
            this.currentPosition++;
            IBusinessHandler nextHandler = this.handlers.get(this.currentPosition - 1);
            return nextHandler.doHandler(param, this);
        }

        return null;
    }

//    private static class VirtualHandlerChain implements IBusinessHandlerChain {
//
//        private final List<? extends IBusinessHandler> additionalHandlers;
//
//        private int currentPosition = 0;
//
//        public VirtualHandlerChain(List<? extends IBusinessHandler> additionalHandlers) {
//            this.additionalHandlers = additionalHandlers;
//        }
//
//        @Override
//        public BusinessHandlerResult executeNextHandler(BusinessHandlerParam param) {
//            if (this.currentPosition != this.additionalHandlers.size()) {
//                this.currentPosition++;
//                IBusinessHandler nextHandler = this.additionalHandlers.get(this.currentPosition - 1);
//                return nextHandler.doHandler(param, this);
//            }
//            return null;
//        }
//
//    }
}
