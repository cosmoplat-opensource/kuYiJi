/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.base.customer;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.micro.application.dto.base.MicroCustomerDTO;
import com.cosmo.hhim.micro.application.service.base.IMicroCustomerFacadeService;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

import java.util.List;

/**
 * 客户基础Controller
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-07
 */
@RestController
@RequestMapping("/base/customer")
public class MicroCustomerController extends BaseController
{

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroCustomerFacadeService microCustomerFacadeService;

    /**
     * 查询客户基础列表
     */
    @GetMapping("/list")
    public TableDataInfo list(MicroCustomerDTO condition)
    {
        startPage();
        List<MicroCustomerDTO> list = microCustomerFacadeService.selectMicroCustomerList(condition);
        return getDataTable(list);
    }


    /**
     * 新增客户基础
     */
    @PostMapping
    public AjaxResult add(@RequestBody MicroCustomerDTO microCustomer)
    {
        return toAjax(microCustomerFacadeService.insertMicroCustomer(microCustomer));
    }

    /**
     * 修改客户基础
     */
    @PutMapping
    public AjaxResult edit(@RequestBody MicroCustomerDTO microCustomer)
    {
        return toAjax(microCustomerFacadeService.updateMicroCustomer(microCustomer));
    }

    /**
     * 删除客户基础
     */
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@ApiParam(name="id",value="客户基础ID")  @PathVariable Long[] ids)
    {
        if (ArrayUtils.isEmpty(ids)){
            return AjaxResult.error("删除的数据不能为空");
        }
        return toAjax(microCustomerFacadeService.deleteMicroCustomerByIds(ids));
    }
}
