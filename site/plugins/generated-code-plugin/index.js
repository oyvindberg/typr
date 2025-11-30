const fs = require('fs');
const path = require('path');

/**
 * Plugin to read generated code files and make them available to the frontend.
 * Strips imports and package declarations, keeping only the core code.
 */
module.exports = function generatedCodePlugin(context, options) {
  const generatedDir = path.join(context.siteDir, 'frontpage-generated');

  function stripImportsAndPackage(code, language) {
    const lines = code.split('\n');
    const result = [];
    let skipNextBlank = false;

    for (const line of lines) {
      // Skip the typo-generated header comment block
      if (line.startsWith('/**') && lines.indexOf(line) < 5) {
        continue;
      }
      if (line.startsWith(' * File has been automatically generated') ||
          line.startsWith(' * IF YOU CHANGE THIS FILE') ||
          (line.startsWith(' */') && result.length === 0)) {
        continue;
      }

      // Skip package and import statements
      if (line.startsWith('package ') || line.startsWith('import ')) {
        skipNextBlank = true;
        continue;
      }

      // Skip blank lines immediately after imports
      if (skipNextBlank && line.trim() === '') {
        continue;
      }
      skipNextBlank = false;

      result.push(line);
    }

    // Remove leading blank lines
    while (result.length > 0 && result[0].trim() === '') {
      result.shift();
    }

    // Remove trailing blank lines
    while (result.length > 0 && result[result.length - 1].trim() === '') {
      result.pop();
    }

    return result.join('\n');
  }

  function readCodeFile(relativePath, language) {
    const fullPath = path.join(generatedDir, language, relativePath);
    try {
      const content = fs.readFileSync(fullPath, 'utf8');
      return stripImportsAndPackage(content, language);
    } catch (e) {
      console.warn(`Could not read ${fullPath}: ${e.message}`);
      return null;
    }
  }

  function buildCodeMap() {
    const codeMap = {};

    // Define which files to read and how to organize them
    const files = [
      // User types
      { key: 'user/UserRow', scala: 'frontpage/frontpage/user/UserRow.scala', java: 'frontpage/frontpage/user/UserRow.java' },
      { key: 'user/UserId', scala: 'frontpage/frontpage/user/UserId.scala', java: 'frontpage/frontpage/user/UserId.java' },
      { key: 'user/UserRepo', scala: 'frontpage/frontpage/user/UserRepo.scala', java: 'frontpage/frontpage/user/UserRepo.java' },
      { key: 'user/UserRepoMock', scala: 'frontpage/frontpage/user/UserRepoMock.scala', java: 'frontpage/frontpage/user/UserRepoMock.java' },
      { key: 'user/UserRowUnsaved', scala: 'frontpage/frontpage/user/UserRowUnsaved.scala', java: 'frontpage/frontpage/user/UserRowUnsaved.java' },
      { key: 'user/UserFields', scala: 'frontpage/frontpage/user/UserFields.scala', java: 'frontpage/frontpage/user/UserFields.java' },

      // Order types
      { key: 'order/OrderRow', scala: 'frontpage/frontpage/order/OrderRow.scala', java: 'frontpage/frontpage/order/OrderRow.java' },
      { key: 'order/OrderId', scala: 'frontpage/frontpage/order/OrderId.scala', java: 'frontpage/frontpage/order/OrderId.java' },
      { key: 'order/OrderRepo', scala: 'frontpage/frontpage/order/OrderRepo.scala', java: 'frontpage/frontpage/order/OrderRepo.java' },
      { key: 'order/OrderFields', scala: 'frontpage/frontpage/order/OrderFields.scala', java: 'frontpage/frontpage/order/OrderFields.java' },

      // Product types
      { key: 'product/ProductRow', scala: 'frontpage/frontpage/product/ProductRow.scala', java: 'frontpage/frontpage/product/ProductRow.java' },
      { key: 'product/ProductId', scala: 'frontpage/frontpage/product/ProductId.scala', java: 'frontpage/frontpage/product/ProductId.java' },
      { key: 'product/ProductRepo', scala: 'frontpage/frontpage/product/ProductRepo.scala', java: 'frontpage/frontpage/product/ProductRepo.java' },
      { key: 'product/ProductFields', scala: 'frontpage/frontpage/product/ProductFields.scala', java: 'frontpage/frontpage/product/ProductFields.java' },

      // Department types
      { key: 'department/DepartmentRow', scala: 'frontpage/frontpage/department/DepartmentRow.scala', java: 'frontpage/frontpage/department/DepartmentRow.java' },
      { key: 'department/DepartmentId', scala: 'frontpage/frontpage/department/DepartmentId.scala', java: 'frontpage/frontpage/department/DepartmentId.java' },
      { key: 'department/DepartmentRepo', scala: 'frontpage/frontpage/department/DepartmentRepo.scala', java: 'frontpage/frontpage/department/DepartmentRepo.java' },

      // Domain types
      { key: 'Email', scala: 'frontpage/frontpage/Email.scala', java: 'frontpage/frontpage/Email.java' },
      { key: 'UserStatus', scala: 'frontpage/frontpage/UserStatus.scala', java: 'frontpage/frontpage/UserStatus.java' },
      { key: 'UserRole', scala: 'frontpage/frontpage/UserRole.scala', java: 'frontpage/frontpage/UserRole.java' },
      { key: 'OrderStatus', scala: 'frontpage/frontpage/OrderStatus.scala', java: 'frontpage/frontpage/OrderStatus.java' },

      // Location (advanced types)
      { key: 'location/LocationRow', scala: 'frontpage/frontpage/location/LocationRow.scala', java: 'frontpage/frontpage/location/LocationRow.java' },
      { key: 'location/LocationId', scala: 'frontpage/frontpage/location/LocationId.scala', java: 'frontpage/frontpage/location/LocationId.java' },

      // Permission (composite key example)
      { key: 'user_permission/UserPermissionRow', scala: 'frontpage/frontpage/user_permission/UserPermissionRow.scala', java: 'frontpage/frontpage/user_permission/UserPermissionRow.java' },

      // Test helpers
      { key: 'TestInsert', scala: 'frontpage/TestInsert.scala', java: 'frontpage/TestInsert.java' },

      // Customer
      { key: 'customer/CustomerRow', scala: 'frontpage/frontpage/customer/CustomerRow.scala', java: 'frontpage/frontpage/customer/CustomerRow.java' },
      { key: 'customer/CustomerFields', scala: 'frontpage/frontpage/customer/CustomerFields.scala', java: 'frontpage/frontpage/customer/CustomerFields.java' },

      // Company
      { key: 'company/CompanyRow', scala: 'frontpage/frontpage/company/CompanyRow.scala', java: 'frontpage/frontpage/company/CompanyRow.java' },

      // Custom types
      { key: 'customtypes/TypoPoint', scala: 'frontpage/customtypes/TypoPoint.scala', java: 'frontpage/customtypes/TypoPoint.java' },
      { key: 'customtypes/TypoPolygon', scala: 'frontpage/customtypes/TypoPolygon.scala', java: 'frontpage/customtypes/TypoPolygon.java' },
      { key: 'customtypes/TypoInet', scala: 'frontpage/customtypes/TypoInet.scala', java: 'frontpage/customtypes/TypoInet.java' },
      { key: 'customtypes/TypoJsonb', scala: 'frontpage/customtypes/TypoJsonb.scala', java: 'frontpage/customtypes/TypoJsonb.java' },
      { key: 'customtypes/TypoLocalDateTime', scala: 'frontpage/customtypes/TypoLocalDateTime.scala', java: 'frontpage/customtypes/TypoLocalDateTime.java' },
    ];

    for (const file of files) {
      codeMap[file.key] = {
        scala: readCodeFile(file.scala, 'scala'),
        java: readCodeFile(file.java, 'java'),
      };
    }

    return codeMap;
  }

  return {
    name: 'generated-code-plugin',

    async loadContent() {
      return buildCodeMap();
    },

    async contentLoaded({ content, actions }) {
      const { setGlobalData } = actions;
      // Make the code map available as global data
      setGlobalData(content);
    },
  };
};
